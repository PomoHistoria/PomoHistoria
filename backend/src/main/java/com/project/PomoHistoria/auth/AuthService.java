package com.project.PomoHistoria.auth;

import com.project.PomoHistoria.common.exception.CredenciaisInvalidasException;
import com.project.PomoHistoria.security.AutenticacaoFactory;
import com.project.PomoHistoria.usuario.Usuario;
import com.project.PomoHistoria.usuario.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioService usuarioService;
    private final LoginAttemptService tentativas;
    private final SecurityContextRepository securityContextRepository;
    private final SecurityContextHolderStrategy contextHolder = SecurityContextHolder.getContextHolderStrategy();

    public AuthService(
            UsuarioService usuarioService,
            LoginAttemptService tentativas,
            SecurityContextRepository securityContextRepository
    ) {
        this.usuarioService = usuarioService;
        this.tentativas = tentativas;
        this.securityContextRepository = securityContextRepository;
    }

    public Usuario entrar(String email, String senha, HttpServletRequest request, HttpServletResponse response) {
        String emailNormalizado = Usuario.normalizarEmail(email);
        String ip = request.getRemoteAddr();
        tentativas.verificarBloqueio(emailNormalizado, ip);

        Usuario usuario;
        try {
            usuario = usuarioService.autenticar(emailNormalizado, senha);
        } catch (CredenciaisInvalidasException exception) {
            tentativas.registrarFalha(emailNormalizado, ip);
            throw exception;
        }
        tentativas.registrarSucesso(emailNormalizado);

        // Troca o ID da sessão no login para evitar fixação de sessão (o token CSRF é mantido).
        request.getSession(true);
        request.changeSessionId();

        SecurityContext contexto = contextHolder.createEmptyContext();
        contexto.setAuthentication(AutenticacaoFactory.de(usuario));
        contextHolder.setContext(contexto);
        securityContextRepository.saveContext(contexto, request, response);
        return usuario;
    }
}
