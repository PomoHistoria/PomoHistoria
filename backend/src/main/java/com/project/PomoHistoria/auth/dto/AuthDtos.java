package com.project.PomoHistoria.auth.dto;

import com.project.PomoHistoria.usuario.SenhaPolicy;
import com.project.PomoHistoria.usuario.dto.UsuarioDtos.UsuarioResponse;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Formatos de entrada e saída dos endpoints de /api/auth. */
public final class AuthDtos {

    private AuthDtos() {
    }

    public record CadastroRequest(
            @NotBlank(message = "Nome é obrigatório.")
            @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
            String nome,

            @NotBlank(message = "Email é obrigatório.")
            @Email(message = "Email inválido.")
            @Size(max = 255, message = "O email deve ter no máximo 255 caracteres.")
            String email,

            @NotBlank(message = "Senha é obrigatória.")
            @Size(
                    min = SenhaPolicy.TAMANHO_MINIMO,
                    max = SenhaPolicy.MAXIMO_BYTES,
                    message = "A senha deve ter entre 8 e 72 caracteres."
            )
            String senha
    ) {
    }

    public record LoginRequest(
            @NotBlank(message = "Email é obrigatório.")
            @Email(message = "Email inválido.")
            @Size(max = 255, message = "O email deve ter no máximo 255 caracteres.")
            String email,

            @NotBlank(message = "Senha é obrigatória.")
            @Size(max = 255, message = "Senha inválida.")
            String senha
    ) {
    }

    public record AuthResponse(boolean sucesso, String mensagem, UsuarioResponse usuario) {
    }

    public record CsrfResponse(String token) {
    }
}
