package com.project.PomoHistoria.security;

import com.project.PomoHistoria.usuario.Usuario;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/** Monta a autenticação usada pela aplicação. O principal é o ID (UUID) do usuário. */
public final class AutenticacaoFactory {

    private AutenticacaoFactory() {
    }

    public static Authentication de(Usuario usuario) {
        return UsernamePasswordAuthenticationToken.authenticated(
                usuario.getId(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getPapel().name()))
        );
    }
}
