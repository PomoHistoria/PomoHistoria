package com.project.PomoHistoria.usuario;

import com.project.PomoHistoria.common.exception.ConflitoException;
import com.project.PomoHistoria.common.exception.CredenciaisInvalidasException;
import com.project.PomoHistoria.common.exception.DadosInvalidosException;
import com.project.PomoHistoria.common.exception.OperacaoNaoPermitidaException;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private static final String MENSAGEM_CREDENCIAIS_INVALIDAS = "Email ou senha inválidos.";
    private static final Pattern FOTO_DATA_URL =
            Pattern.compile("^data:image/(?:jpeg|png|webp);base64,([A-Za-z0-9+/]+={0,2})$");

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    /** Hash usado quando o e-mail não existe, para o tempo de resposta não revelar contas cadastradas. */
    private final String hashFicticio;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.hashFicticio = passwordEncoder.encode("senha-ficticia-para-equalizar-tempo");
    }

    public Usuario cadastrar(String nome, String email, String senha) {
        SenhaPolicy.validarNovaSenha(senha);
        String emailNormalizado = Usuario.normalizarEmail(email);
        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new ConflitoException("Email já cadastrado.");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(nome.trim());
        usuario.setEmail(emailNormalizado);
        usuario.setSenha(passwordEncoder.encode(senha));
        usuario.setAtivo(true);
        usuario.setPapel(Usuario.Papel.USER);

        try {
            return usuarioRepository.save(usuario);
        } catch (DataIntegrityViolationException exception) {
            // Dois cadastros simultâneos com o mesmo e-mail.
            throw new ConflitoException("Email já cadastrado.");
        }
    }

    /**
     * Atualiza nome, telefone, e-mail e (opcionalmente) a senha.
     * Trocar e-mail ou senha exige a senha atual, para quem pegar uma sessão aberta não sequestrar a conta.
     */
    public Usuario atualizarPerfil(
            UUID id,
            String nome,
            String telefone,
            String email,
            String senhaAtual,
            String novaSenha
    ) {
        Usuario usuario = buscarAtivo(id);
        String emailNovo = Usuario.normalizarEmail(email);
        boolean trocaEmail = !emailNovo.equals(usuario.getEmail());
        boolean trocaSenha = novaSenha != null && !novaSenha.isEmpty();

        if ((trocaEmail || trocaSenha) && !senhaAtualConfere(usuario, senhaAtual)) {
            // 400, não 401: o front trata 401 como "sessão expirada".
            throw new DadosInvalidosException("Senha atual incorreta. Ela é necessária para trocar email ou senha.");
        }
        if (trocaEmail && usuarioRepository.existsByEmail(emailNovo)) {
            throw new ConflitoException("Email já cadastrado.");
        }
        if (trocaSenha) {
            SenhaPolicy.validarNovaSenha(novaSenha);
            usuario.setSenha(passwordEncoder.encode(novaSenha));
        }

        usuario.setNome(nome.trim());
        usuario.setTelefone(telefone == null || telefone.isBlank() ? null : telefone.trim());
        usuario.setEmail(emailNovo);
        try {
            return usuarioRepository.saveAndFlush(usuario);
        } catch (DataIntegrityViolationException exception) {
            throw new ConflitoException("Email já cadastrado.");
        }
    }

    /**
     * Exclui a própria conta, depois de conferir a senha atual. O progresso e os módulos iniciados
     * são apagados junto pelo banco (ON DELETE CASCADE).
     * Contas ADMIN não podem ser excluídas por aqui, para o sistema não ficar sem quem edite as questões.
     */
    public void excluirConta(UUID id, String senhaAtual) {
        Usuario usuario = buscarAtivo(id);
        if (usuario.getPapel() == Usuario.Papel.ADMIN) {
            throw new OperacaoNaoPermitidaException("Contas de administrador não podem ser excluídas pelo perfil.");
        }
        if (!senhaAtualConfere(usuario, senhaAtual)) {
            throw new DadosInvalidosException("Senha atual incorreta.");
        }
        usuarioRepository.delete(usuario);
        usuarioRepository.flush();
    }

    private boolean senhaAtualConfere(Usuario usuario, String senhaAtual) {
        return senhaAtual != null
                && !senhaAtual.isEmpty()
                && !SenhaPolicy.excedeLimiteDeBytes(senhaAtual)
                && passwordEncoder.matches(senhaAtual, usuario.getSenha());
    }

    /** {@code foto} nula remove a foto. Aceita só data URL de imagem JPEG, PNG ou WebP. */
    public Usuario atualizarFoto(UUID id, String foto) {
        Usuario usuario = buscarAtivo(id);
        if (foto != null) {
            validarFoto(foto);
        }
        usuario.setFoto(foto);
        return usuarioRepository.saveAndFlush(usuario);
    }

    private static void validarFoto(String foto) {
        Matcher matcher = FOTO_DATA_URL.matcher(foto);
        if (!matcher.matches()) {
            throw new DadosInvalidosException("Formato de foto inválido. Use JPEG, PNG ou WebP.");
        }
        try {
            Base64.getDecoder().decode(matcher.group(1));
        } catch (IllegalArgumentException exception) {
            throw new DadosInvalidosException("Formato de foto inválido. Use JPEG, PNG ou WebP.");
        }
    }

    public Usuario buscarAtivo(UUID id) {
        return usuarioRepository.findById(id)
                .filter(Usuario::isAtivo)
                .orElseThrow(() -> new CredenciaisInvalidasException("Faça login para continuar."));
    }

    public Usuario autenticar(String email, String senha) {
        if (SenhaPolicy.excedeLimiteDeBytes(senha)) {
            throw new CredenciaisInvalidasException(MENSAGEM_CREDENCIAIS_INVALIDAS);
        }

        Usuario usuario = usuarioRepository.findByEmail(Usuario.normalizarEmail(email)).orElse(null);
        if (usuario == null) {
            passwordEncoder.matches(senha, hashFicticio);
            throw new CredenciaisInvalidasException(MENSAGEM_CREDENCIAIS_INVALIDAS);
        }

        boolean senhaCorreta = passwordEncoder.matches(senha, usuario.getSenha());
        if (!senhaCorreta || !usuario.isAtivo()) {
            throw new CredenciaisInvalidasException(MENSAGEM_CREDENCIAIS_INVALIDAS);
        }
        return usuario;
    }
}
