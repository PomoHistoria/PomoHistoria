package com.project.PomoHistoria.auth;

import com.project.PomoHistoria.common.exception.CredenciaisInvalidasException;
import com.project.PomoHistoria.security.AutenticacaoFactory;
import com.project.PomoHistoria.usuario.Usuario;
import com.project.PomoHistoria.usuario.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

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
        registrarOrigemTemporario(request);
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

    /**
     * TEMPORÁRIO: mostra nos logs de onde o login chega, para descobrir quantos proxies o Render põe
     * no caminho depois do rewrite de /api no frontend. Remover depois de ajustar a leitura do IP.
     */
    private static void registrarOrigemTemporario(HttpServletRequest request) {
        log.info(
                "[TEMP-IP] remoteAddr={} x-forwarded-for={} true-client-ip={} cf-connecting-ip={} x-real-ip={}",
                request.getRemoteAddr(),
                limparParaLog(request.getHeader("X-Forwarded-For")),
                limparParaLog(request.getHeader("True-Client-IP")),
                limparParaLog(request.getHeader("CF-Connecting-IP")),
                limparParaLog(request.getHeader("X-Real-IP"))
        );
    }

    /** Headers vêm do cliente: corta quebras de linha (falsificação de log) e limita o tamanho. */
    private static String limparParaLog(String valor) {
        if (valor == null) {
            return "-";
        }
        String limpo = valor.replaceAll("[\\r\\n]", "_");
        return limpo.length() > 200 ? limpo.substring(0, 200) + "…" : limpo;
    }
}
