package com.project.PomoHistoria.security;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.project.PomoHistoria.questionario.ModuloIniciadoRepository;
import com.project.PomoHistoria.support.PostgresEmbutido;
import com.project.PomoHistoria.usuario.Usuario;
import com.project.PomoHistoria.usuario.UsuarioRepository;
import java.lang.reflect.Constructor;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/** Reproduz o fluxo do frontend: pega o token CSRF, envia no header X-XSRF-TOKEN e usa o cookie de sessão. */
@SpringBootTest(properties = {
        "app.cors.allowed-origins=https://front.exemplo.com/",
        "app.security.login.max-falhas-por-ip=1000"
})
@AutoConfigureMockMvc
class SegurancaIntegrationTest extends PostgresEmbutido {

    private static final String SENHA = "senha-forte-123";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ModuloIniciadoRepository moduloIniciadoRepository;

    @Test
    void usuarioComumExcluiAPropriaContaComSenha() throws Exception {
        MockHttpSession sessao = usuarioLogado(Usuario.Papel.USER);
        String email = (String) sessao.getAttribute("email-teste");
        UUID id = usuarioRepository.findByEmail(email).orElseThrow().getId();
        comCsrf(sessao, post("/api/questionario/modulos/1/iniciar"), null).andExpect(status().isNoContent());
        assertTrue(moduloIniciadoRepository.existsByUsuarioIdAndModulo(id, 1));

        comCsrf(sessao, delete("/api/usuarios/me"), "{\"senhaAtual\":\"senha-errada\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Senha atual incorreta."));
        comCsrf(sessao, delete("/api/usuarios/me"), "{}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Informe sua senha para excluir a conta."));
        assertTrue(usuarioRepository.findByEmail(email).isPresent(), "Senha errada não pode excluir");

        comCsrf(sessao, delete("/api/usuarios/me"), "{\"senhaAtual\":\"" + SENHA + "\"}")
                .andExpect(status().isNoContent());

        assertTrue(usuarioRepository.findByEmail(email).isEmpty(), "A conta deve ser apagada");
        assertFalse(moduloIniciadoRepository.existsByUsuarioIdAndModulo(id, 1), "O progresso deve ir junto");
        mvc.perform(get("/api/auth/me").session(sessao)).andExpect(status().isUnauthorized());
        entrar(new MockHttpSession(), email, SENHA).andExpect(status().isUnauthorized());
    }

    @Test
    void adminNaoExcluiAPropriaConta() throws Exception {
        MockHttpSession sessao = usuarioLogado(Usuario.Papel.ADMIN);
        String email = (String) sessao.getAttribute("email-teste");

        comCsrf(sessao, delete("/api/usuarios/me"), "{\"senhaAtual\":\"" + SENHA + "\"}")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Contas de administrador não podem ser excluídas pelo perfil."));

        assertTrue(usuarioRepository.findByEmail(email).isPresent());
        mvc.perform(get("/api/auth/me").session(sessao)).andExpect(status().isOk());
    }

    @Test
    void exclusaoDeContaExigeTokenCsrf() throws Exception {
        MockHttpSession sessao = usuarioLogado(Usuario.Papel.USER);
        mvc.perform(delete("/api/usuarios/me")
                        .session(sessao)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senhaAtual\":\"" + SENHA + "\"}"))
                .andExpect(status().isForbidden());
        assertTrue(usuarioRepository.findByEmail((String) sessao.getAttribute("email-teste")).isPresent());
    }

    @Test
    void healthCheckEPublico() throws Exception {
        mvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    void anonimoRecebe401EmJson() throws Exception {
        mvc.perform(get("/api/questionario/progresso"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Faça login para continuar."));
    }

    @Test
    void postSemTokenCsrfERecusado() throws Exception {
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cadastroJson("Teste", emailNovo(), SENHA)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("Sessão de segurança")));
    }

    @Test
    void fluxoCompletoCadastroLoginAcessoELogout() throws Exception {
        MockHttpSession sessao = new MockHttpSession();
        String email = emailNovo();

        comCsrf(sessao, post("/api/auth/register"), cadastroJson("Maria", email, SENHA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuario.email").value(email))
                .andExpect(jsonPath("$.usuario.papel").value("USER"))
                .andExpect(jsonPath("$.usuario.senha").doesNotExist());

        mvc.perform(get("/api/auth/me").session(sessao)).andExpect(status().isUnauthorized());

        String idAntesDoLogin = sessao.getId();
        entrar(sessao, email, SENHA).andExpect(status().isOk());
        assertNotEquals(idAntesDoLogin, sessao.getId(), "O ID da sessão deve mudar no login");

        mvc.perform(get("/api/auth/me").session(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Maria"))
                .andExpect(jsonPath("$.email").value(email));

        mvc.perform(get("/api/questionario/progresso").session(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.moduloAtual").value(1));
        mvc.perform(get("/api/questionario/modulos/1/partes/1").session(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].respostaCorreta").doesNotExist());

        comCsrf(sessao, post("/api/auth/logout"), null).andExpect(status().isNoContent());
        mvc.perform(get("/api/questionario/progresso").session(sessao))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void contaDesativadaPerdeAcessoNaHora() throws Exception {
        MockHttpSession sessao = usuarioLogado(Usuario.Papel.USER);
        mvc.perform(get("/api/questionario/progresso").session(sessao)).andExpect(status().isOk());

        Usuario usuario = usuarioRepository.findAll().stream()
                .filter(u -> u.getEmail().equals(sessao.getAttribute("email-teste")))
                .findFirst()
                .orElseThrow();
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);

        mvc.perform(get("/api/questionario/progresso").session(sessao)).andExpect(status().isUnauthorized());
    }

    @Test
    void usuarioComumNaoAcessaRotasDeAdmin() throws Exception {
        MockHttpSession sessao = usuarioLogado(Usuario.Papel.USER);
        mvc.perform(get("/api/admin/questoes").session(sessao))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("permissão")));
        comCsrf(sessao, put("/api/admin/questoes/" + UUID.randomUUID()), questaoEdicaoJson("Texto", "A"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminListaComGabaritoEEditaQuestao() throws Exception {
        MockHttpSession admin = usuarioLogado(Usuario.Papel.ADMIN);
        String lista = mvc.perform(get("/api/admin/questoes").session(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(24))
                .andExpect(jsonPath("$[0].modulo").value(1))
                .andExpect(jsonPath("$[0].respostaCorreta").exists())
                .andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(lista, "$[0].id");
        String gabarito = JsonPath.read(lista, "$[0].respostaCorreta");

        comCsrf(admin, put("/api/admin/questoes/" + id), questaoEdicaoJson("Enunciado editado pelo admin", gabarito))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enunciado").value("Enunciado editado pelo admin"))
                .andExpect(jsonPath("$.modulo").value(1));

        // O usuário vê o texto novo, mas continua sem o gabarito.
        MockHttpSession usuario = usuarioLogado(Usuario.Papel.USER);
        mvc.perform(get("/api/questionario/modulos/1/partes/1").session(usuario))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].enunciado").value("Enunciado editado pelo admin"))
                .andExpect(jsonPath("$[0].respostaCorreta").doesNotExist());
    }

    @Test
    void edicaoDeQuestaoValidaDadosEId() throws Exception {
        MockHttpSession admin = usuarioLogado(Usuario.Papel.ADMIN);
        comCsrf(admin, put("/api/admin/questoes/" + UUID.randomUUID()), questaoEdicaoJson("Texto", "A"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Questão não encontrada."));
        comCsrf(admin, put("/api/admin/questoes/" + UUID.randomUUID()), questaoEdicaoJson("Texto", "F"))
                .andExpect(status().isBadRequest());
        comCsrf(admin, put("/api/admin/questoes/" + UUID.randomUUID()), questaoEdicaoJson(" ", "A"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void perfilSalvaDadosETrocaDeEmailExigeSenhaAtual() throws Exception {
        MockHttpSession sessao = usuarioLogado(Usuario.Papel.USER);
        String email = (String) sessao.getAttribute("email-teste");

        comCsrf(sessao, put("/api/usuarios/me"), perfilJson("Nome Novo", "(11) 98888-7777", email, null, null))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Nome Novo"))
                .andExpect(jsonPath("$.telefone").value("(11) 98888-7777"));
        mvc.perform(get("/api/auth/me").session(sessao))
                .andExpect(jsonPath("$.nome").value("Nome Novo"));

        String emailNovo = emailNovo();
        comCsrf(sessao, put("/api/usuarios/me"), perfilJson("Nome Novo", "", emailNovo, null, null))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Senha atual incorreta")));
        comCsrf(sessao, put("/api/usuarios/me"), perfilJson("Nome Novo", "", emailNovo, SENHA, "nova-senha-456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(emailNovo))
                .andExpect(jsonPath("$.telefone").doesNotExist());

        entrar(new MockHttpSession(), emailNovo, "nova-senha-456").andExpect(status().isOk());
        entrar(new MockHttpSession(), emailNovo, SENHA).andExpect(status().isUnauthorized());
    }

    @Test
    void perfilRecusaTelefoneInvalidoEEmailDeOutraConta() throws Exception {
        MockHttpSession outra = usuarioLogado(Usuario.Papel.USER);
        String emailDaOutra = (String) outra.getAttribute("email-teste");
        MockHttpSession sessao = usuarioLogado(Usuario.Papel.USER);
        String email = (String) sessao.getAttribute("email-teste");

        comCsrf(sessao, put("/api/usuarios/me"), perfilJson("Ana", "abc", email, null, null))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Telefone inválido."));
        comCsrf(sessao, put("/api/usuarios/me"), perfilJson("Ana", "", emailDaOutra, SENHA, null))
                .andExpect(status().isConflict());
        comCsrf(sessao, put("/api/usuarios/me"), perfilJson("Ana", "", email, SENHA, "curta"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void fotoDePerfilESalvaEValidada() throws Exception {
        MockHttpSession sessao = usuarioLogado(Usuario.Papel.USER);
        String foto = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAEBAQ==";

        comCsrf(sessao, put("/api/usuarios/me/foto"), "{\"foto\":\"" + foto + "\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.foto").value(foto));
        mvc.perform(get("/api/auth/me").session(sessao)).andExpect(jsonPath("$.foto").value(foto));

        comCsrf(sessao, put("/api/usuarios/me/foto"), "{\"foto\":\"javascript:alert(1)\"}")
                .andExpect(status().isBadRequest());
        comCsrf(sessao, put("/api/usuarios/me/foto"), "{\"foto\":\"data:image/svg+xml;base64,PHN2Zz4=\"}")
                .andExpect(status().isBadRequest());
        comCsrf(sessao, put("/api/usuarios/me/foto"), "{\"foto\":null}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.foto").doesNotExist());
    }

    @Test
    void registraModuloIniciadoSomenteSeLiberado() throws Exception {
        MockHttpSession sessao = usuarioLogado(Usuario.Papel.USER);
        mvc.perform(get("/api/questionario/progresso").session(sessao))
                .andExpect(jsonPath("$.modulosIniciados.length()").value(0));

        comCsrf(sessao, post("/api/questionario/modulos/1/iniciar"), null).andExpect(status().isNoContent());
        comCsrf(sessao, post("/api/questionario/modulos/1/iniciar"), null).andExpect(status().isNoContent());
        comCsrf(sessao, post("/api/questionario/modulos/2/iniciar"), null).andExpect(status().isForbidden());
        comCsrf(sessao, post("/api/questionario/modulos/3/iniciar"), null).andExpect(status().isBadRequest());

        mvc.perform(get("/api/questionario/progresso").session(sessao))
                .andExpect(jsonPath("$.modulosIniciados.length()").value(1))
                .andExpect(jsonPath("$.modulosIniciados[0]").value(1));
    }

    @Test
    void rotaAntigaDeCriarQuestaoNaoExisteMais() throws Exception {
        MockHttpSession admin = usuarioLogado(Usuario.Papel.ADMIN);
        comCsrf(admin, post("/api/questionario"), questaoEdicaoJson("Texto", "A"))
                .andExpect(status().isNotFound());
    }

    @Test
    void bloqueiaLoginDepoisDeCincoSenhasErradas() throws Exception {
        MockHttpSession sessao = new MockHttpSession();
        String email = emailNovo();
        comCsrf(sessao, post("/api/auth/register"), cadastroJson("Ana", email, SENHA)).andExpect(status().isCreated());

        for (int tentativa = 0; tentativa < 5; tentativa++) {
            entrar(sessao, email, "senha-errada-123")
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Email ou senha inválidos."));
        }
        entrar(sessao, email, SENHA).andExpect(status().isTooManyRequests());
    }

    @Test
    void loginComEmailInexistenteTemMesmaRespostaQueSenhaErrada() throws Exception {
        entrar(new MockHttpSession(), emailNovo(), SENHA)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Email ou senha inválidos."));
    }

    @Test
    void recusaSenhaCurtaESenhaAcimaDe72Bytes() throws Exception {
        MockHttpSession sessao = new MockHttpSession();
        comCsrf(sessao, post("/api/auth/register"), cadastroJson("Ana", emailNovo(), "1234567"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A senha deve ter entre 8 e 72 caracteres."));

        String senhaComAcentos = "é".repeat(40); // 40 caracteres, 80 bytes
        comCsrf(sessao, post("/api/auth/register"), cadastroJson("Ana", emailNovo(), senhaComAcentos))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("longa demais")));
    }

    @Test
    void emailDuplicadoRetorna409() throws Exception {
        MockHttpSession sessao = new MockHttpSession();
        String email = emailNovo();
        comCsrf(sessao, post("/api/auth/register"), cadastroJson("Ana", email, SENHA)).andExpect(status().isCreated());
        comCsrf(sessao, post("/api/auth/register"), cadastroJson("Ana", email.toUpperCase(), SENHA))
                .andExpect(status().isConflict());
    }

    @Test
    void jsonMalFormadoERotaInexistenteRetornamErroLimpo() throws Exception {
        MockHttpSession sessao = new MockHttpSession();
        comCsrf(sessao, post("/api/auth/login"), "{nao e json")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Requisição inválida."));

        MockHttpSession logado = usuarioLogado(Usuario.Papel.USER);
        mvc.perform(get("/api/rota-que-nao-existe").session(logado))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Recurso não encontrado."));
    }

    @Test
    void corsAceitaSomenteOrigemConfigurada() throws Exception {
        mvc.perform(options("/api/auth/login")
                        .header("Origin", "https://front.exemplo.com")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type,x-xsrf-token"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://front.exemplo.com"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));

        mvc.perform(options("/api/auth/login")
                        .header("Origin", "https://site-malicioso.com")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }

    // ---- auxiliares ----

    private MockHttpSession usuarioLogado(Usuario.Papel papel) throws Exception {
        String email = emailNovo();
        Usuario usuario = novoUsuario();
        usuario.setNome("Teste " + papel);
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(SENHA));
        usuario.setAtivo(true);
        usuario.setPapel(papel);
        usuarioRepository.save(usuario);

        MockHttpSession sessao = new MockHttpSession();
        entrar(sessao, email, SENHA).andExpect(status().isOk());
        sessao.setAttribute("email-teste", email);
        return sessao;
    }

    private ResultActions entrar(MockHttpSession sessao, String email, String senha) throws Exception {
        return comCsrf(sessao, post("/api/auth/login"),
                "{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}");
    }

    private ResultActions comCsrf(
            MockHttpSession sessao,
            org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder requisicao,
            String corpoJson
    ) throws Exception {
        String resposta = mvc.perform(get("/api/auth/csrf").session(sessao))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(resposta, "$.token");

        requisicao.session(sessao).header(SecurityConfig.CSRF_HEADER, token);
        if (corpoJson != null) {
            requisicao.contentType(MediaType.APPLICATION_JSON).content(corpoJson);
        }
        return mvc.perform(requisicao);
    }

    private static String cadastroJson(String nome, String email, String senha) {
        return "{\"nome\":\"" + nome + "\",\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}";
    }

    private static String questaoEdicaoJson(String enunciado, String respostaCorreta) {
        return "{\"enunciado\":\"" + enunciado + "\",\"alternativaA\":\"a\",\"alternativaB\":\"b\","
                + "\"alternativaC\":\"c\",\"alternativaD\":\"d\",\"alternativaE\":\"e\","
                + "\"respostaCorreta\":\"" + respostaCorreta + "\"}";
    }

    private static String perfilJson(String nome, String telefone, String email, String senhaAtual, String novaSenha) {
        return "{\"nome\":\"" + nome + "\",\"telefone\":\"" + telefone + "\",\"email\":\"" + email + "\","
                + "\"senhaAtual\":" + (senhaAtual == null ? "null" : "\"" + senhaAtual + "\"") + ","
                + "\"novaSenha\":" + (novaSenha == null ? "null" : "\"" + novaSenha + "\"") + "}";
    }

    private static String emailNovo() {
        return "teste-" + UUID.randomUUID() + "@exemplo.com";
    }

    /** O construtor de Usuario é protegido (uso do JPA); no teste criamos via reflexão. */
    private static Usuario novoUsuario() throws Exception {
        Constructor<Usuario> construtor = Usuario.class.getDeclaredConstructor();
        construtor.setAccessible(true);
        return construtor.newInstance();
    }
}
