package com.project.PomoHistoria.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfException;

/** Responde 401/403 no mesmo formato JSON do resto da API: {"message": "..."}. */
public class JsonSecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {
        escrever(response, HttpStatus.UNAUTHORIZED, "Faça login para continuar.");
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exception
    ) throws IOException {
        String mensagem = exception instanceof CsrfException
                ? "Sessão de segurança expirada. Recarregue a página e tente novamente."
                : "Você não tem permissão para realizar esta operação.";
        escrever(response, HttpStatus.FORBIDDEN, mensagem);
    }

    // As mensagens são fixas e não têm aspas, então não precisam de escape JSON.
    private static void escrever(HttpServletResponse response, HttpStatus status, String mensagem) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        response.setStatus(status.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"message\":\"" + mensagem + "\"}");
    }
}
