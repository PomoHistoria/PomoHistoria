package com.project.PomoHistoria.security;

import com.project.PomoHistoria.usuario.Usuario;
import com.project.PomoHistoria.usuario.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * A sessão guarda apenas quem fez login. A cada requisição, este filtro confere no banco se a conta
 * ainda existe e está ativa, e aplica o papel atual. Assim, desativar ou rebaixar um usuário vale na
 * hora, sem esperar a sessão expirar.
 */
public class UsuarioAutenticadoFilter extends OncePerRequestFilter {

    private final UsuarioRepository usuarioRepository;
    private final SecurityContextHolderStrategy contextHolder = SecurityContextHolder.getContextHolderStrategy();

    public UsuarioAutenticadoFilter(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Authentication autenticacao = contextHolder.getContext().getAuthentication();
        if (autenticacao != null && autenticacao.getPrincipal() instanceof UUID usuarioId) {
            Optional<Usuario> usuario = usuarioRepository.findById(usuarioId).filter(Usuario::isAtivo);
            if (usuario.isPresent()) {
                SecurityContext contexto = contextHolder.createEmptyContext();
                contexto.setAuthentication(AutenticacaoFactory.de(usuario.get()));
                contextHolder.setContext(contexto);
            } else {
                contextHolder.clearContext();
                HttpSession sessao = request.getSession(false);
                if (sessao != null) {
                    sessao.invalidate();
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
