package com.project.PomoHistoria.usuario;

import com.project.PomoHistoria.usuario.dto.UsuarioDtos.FotoRequest;
import com.project.PomoHistoria.usuario.dto.UsuarioDtos.PerfilRequest;
import com.project.PomoHistoria.usuario.dto.UsuarioDtos.UsuarioResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Perfil do usuário logado. Para ler os dados, use GET /api/auth/me. */
@RestController
@RequestMapping("/api/usuarios/me")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PutMapping
    public UsuarioResponse atualizarPerfil(
            @AuthenticationPrincipal UUID usuarioId,
            @Valid @RequestBody PerfilRequest request
    ) {
        return UsuarioResponse.de(usuarioService.atualizarPerfil(
                usuarioId,
                request.nome(),
                request.telefone(),
                request.email(),
                request.senhaAtual(),
                request.novaSenha()
        ));
    }

    @PutMapping("/foto")
    public UsuarioResponse atualizarFoto(
            @AuthenticationPrincipal UUID usuarioId,
            @Valid @RequestBody FotoRequest request
    ) {
        return UsuarioResponse.de(usuarioService.atualizarFoto(usuarioId, request.foto()));
    }
}
