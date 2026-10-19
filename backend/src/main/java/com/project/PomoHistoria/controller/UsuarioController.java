package com.project.PomoHistoria.controller;

import com.project.PomoHistoria.model.Usuario;
import com.project.PomoHistoria.model.UsuarioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> cadastrar(@Valid @RequestBody CadastroRequest request) {
        Usuario usuario = usuarioService.cadastrar(request.nome(), request.email(), request.senha());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(true, "Cadastro realizado com sucesso.", UsuarioResponse.de(usuario)));
    }

    @PostMapping("/login")
    public AuthResponse autenticar(@Valid @RequestBody LoginRequest request) {
        Usuario usuario = usuarioService.autenticar(request.email(), request.senha());
        return new AuthResponse(true, "Login realizado com sucesso.", UsuarioResponse.de(usuario));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> tratarErroDeAutenticacao(ResponseStatusException exception) {
        String mensagem = exception.getReason() == null ? "Não foi possível concluir a operação." : exception.getReason();
        return ResponseEntity.status(exception.getStatusCode())
                .body(new ErrorResponse(mensagem, mensagem));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> tratarErroDeValidacao(MethodArgumentNotValidException exception) {
        FieldError erro = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .orElse(null);
        String mensagem = erro == null ? "Dados inválidos." : erro.getDefaultMessage();
        return ResponseEntity.badRequest().body(new ErrorResponse(mensagem, mensagem));
    }

    public record CadastroRequest(
            @NotBlank @Size(max = 100) String nome,
            @NotBlank @Email @Size(max = 255) String email,
            @NotBlank @Size(min = 6, max = 72) String senha
    ) {
    }

    public record LoginRequest(
            @NotBlank @Email @Size(max = 255) String email,
            @NotBlank String senha
    ) {
    }

    public record AuthResponse(boolean sucesso, String mensagem, UsuarioResponse usuario) {
    }

    public record UsuarioResponse(UUID id, String nome, String email, Instant dataCriacao, boolean ativo) {

        private static UsuarioResponse de(Usuario usuario) {
            return new UsuarioResponse(
                    usuario.getId(),
                    usuario.getNome(),
                    usuario.getEmail(),
                    usuario.getDataCriacao(),
                    usuario.isAtivo()
            );
        }
    }

    public record ErrorResponse(String message, String mensagem) {
    }
}
