package com.project.PomoHistoria.usuario;

import com.project.PomoHistoria.usuario.dto.UsuarioDtos.ExclusaoContaRequest;
import com.project.PomoHistoria.usuario.dto.UsuarioDtos.FotoRequest;
import com.project.PomoHistoria.usuario.dto.UsuarioDtos.PerfilRequest;
import com.project.PomoHistoria.usuario.dto.UsuarioDtos.UsuarioResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Perfil e conta do usuário logado. Para ler os dados, use GET /api/auth/me. */
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

    /** Exclui a própria conta (só usuários comuns) e encerra a sessão. */
    @DeleteMapping
    public ResponseEntity<Void> excluirConta(
            @AuthenticationPrincipal UUID usuarioId,
            @Valid @RequestBody ExclusaoContaRequest request,
            HttpServletRequest httpRequest
    ) {
        usuarioService.excluirConta(usuarioId, request.senhaAtual());
        SecurityContextHolder.clearContext();
        HttpSession sessao = httpRequest.getSession(false);
        if (sessao != null) {
            sessao.invalidate();
        }
        return ResponseEntity.noContent().build();
    }
}
