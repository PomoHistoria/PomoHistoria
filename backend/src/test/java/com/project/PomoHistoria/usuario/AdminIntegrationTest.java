package com.project.PomoHistoria.usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.project.PomoHistoria.security.SecurityConfig;
import com.project.PomoHistoria.support.PostgresEmbutido;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/** Sobe a aplicação com ADMIN_EMAIL/ADMIN_PASSWORD definidos, como no Render. */
@SpringBootTest(properties = {
        "app.admin.email= Admin@PomoHistoria.com ",
        "app.admin.password=Senha-Admin-Forte-2026"
})
@AutoConfigureMockMvc
class AdminIntegrationTest extends PostgresEmbutido {

    private static final String EMAIL = "admin@pomohistoria.com";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AdminAccountInitializer initializer;

    @Test
    void contaAdminECriadaNaInicializacaoComEmailNormalizado() {
        Usuario admin = usuarioRepository.findByEmail(EMAIL).orElseThrow();
        assertEquals(Usuario.Papel.ADMIN, admin.getPapel());
        assertEquals(true, admin.isAtivo());
    }

    @Test
    void reiniciarNaoDuplicaNemAlteraAConta() throws Exception {
        long antes = usuarioRepository.count();
        initializer.run(new DefaultApplicationArguments(new String[0]));
        assertEquals(antes, usuarioRepository.count());
    }

    @Test
    void adminFazLoginEPassaPelaRotaExclusiva() throws Exception {
        MockHttpSession sessao = new MockHttpSession();
        comCsrf(sessao, post("/api/auth/login"),
                "{\"email\":\"Admin@PomoHistoria.com\",\"senha\":\"Senha-Admin-Forte-2026\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.papel").value("ADMIN"));

        mvc.perform(get("/api/auth/me").session(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.papel").value("ADMIN"));

        mvc.perform(get("/api/admin/questoes").session(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(24));
    }

    @Test
    void senhaErradaDoAdminERecusada() throws Exception {
        comCsrf(new MockHttpSession(), post("/api/auth/login"),
                "{\"email\":\"" + EMAIL + "\",\"senha\":\"senha-errada\"}")
                .andExpect(status().isUnauthorized());
    }

    private ResultActions comCsrf(
            MockHttpSession sessao,
            org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder requisicao,
            String corpoJson
    ) throws Exception {
        String resposta = mvc.perform(get("/api/auth/csrf").session(sessao))
                .andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(resposta, "$.token");
        return mvc.perform(requisicao.session(sessao)
                .header(SecurityConfig.CSRF_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpoJson));
    }
}
