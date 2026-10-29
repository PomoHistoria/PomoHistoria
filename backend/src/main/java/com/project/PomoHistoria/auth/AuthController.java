package com.project.PomoHistoria.auth;

import com.project.PomoHistoria.auth.dto.AuthDtos.AuthResponse;
import com.project.PomoHistoria.auth.dto.AuthDtos.CadastroRequest;
import com.project.PomoHistoria.auth.dto.AuthDtos.CsrfResponse;
import com.project.PomoHistoria.auth.dto.AuthDtos.LoginRequest;
import com.project.PomoHistoria.usuario.dto.UsuarioDtos.UsuarioResponse;
import com.project.PomoHistoria.usuario.Usuario;
import com.project.PomoHistoria.usuario.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** O logout (POST /api/auth/logout) é tratado pelo Spring Security, configurado em SecurityConfig. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;
    private final AuthService authService;

    public AuthController(UsuarioService usuarioService, AuthService authService) {
        this.usuarioService = usuarioService;
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> cadastrar(@Valid @RequestBody CadastroRequest request) {
        Usuario usuario = usuarioService.cadastrar(request.nome(), request.email(), request.senha());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(true, "Cadastro realizado com sucesso.", UsuarioResponse.de(usuario)));
    }

    @PostMapping("/login")
    public AuthResponse entrar(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        Usuario usuario = authService.entrar(request.email(), request.senha(), httpRequest, httpResponse);
        return new AuthResponse(true, "Login realizado com sucesso.", UsuarioResponse.de(usuario));
    }

    /** Usuário da sessão atual. Responde 401 se não houver login. */
    @GetMapping("/me")
    public UsuarioResponse usuarioAtual(@AuthenticationPrincipal UUID usuarioId) {
        return UsuarioResponse.de(usuarioService.buscarAtivo(usuarioId));
    }

    @GetMapping("/csrf")
    public CsrfResponse csrf(CsrfToken token) {
        return new CsrfResponse(token.getToken());
    }
}
