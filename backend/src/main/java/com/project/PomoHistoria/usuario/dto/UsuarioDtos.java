package com.project.PomoHistoria.usuario.dto;

import com.project.PomoHistoria.usuario.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

/** Formatos de entrada e saída dos dados do usuário. */
public final class UsuarioDtos {

    /** Tamanho máximo da data URL da foto (a imagem chega reduzida a 256x256, ~30 KB). */
    public static final int FOTO_MAX_CARACTERES = 400_000;

    private UsuarioDtos() {
    }

    public record UsuarioResponse(
            UUID id,
            String nome,
            String email,
            String telefone,
            String foto,
            Instant dataCriacao,
            boolean ativo,
            Usuario.Papel papel
    ) {

        public static UsuarioResponse de(Usuario usuario) {
            return new UsuarioResponse(
                    usuario.getId(),
                    usuario.getNome(),
                    usuario.getEmail(),
                    usuario.getTelefone(),
                    usuario.getFoto(),
                    usuario.getDataCriacao(),
                    usuario.isAtivo(),
                    usuario.getPapel()
            );
        }
    }

    /**
     * Atualização do perfil. {@code senhaAtual} só é exigida quando o e-mail muda ou
     * quando {@code novaSenha} é enviada.
     */
    public record PerfilRequest(
            @NotBlank(message = "Nome é obrigatório.")
            @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
            String nome,

            @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres.")
            @Pattern(regexp = "^$|^[0-9()+\\-\\s]{8,20}$", message = "Telefone inválido.")
            String telefone,

            @NotBlank(message = "Email é obrigatório.")
            @Email(message = "Email inválido.")
            @Size(max = 255, message = "O email deve ter no máximo 255 caracteres.")
            String email,

            @Size(max = 255, message = "Senha atual inválida.")
            String senhaAtual,

            @Size(max = 72, message = "A nova senha deve ter entre 8 e 72 caracteres.")
            String novaSenha
    ) {
    }

    /** {@code foto} nula remove a foto atual. */
    public record FotoRequest(
            @Size(max = FOTO_MAX_CARACTERES, message = "A foto é grande demais.")
            String foto
    ) {
    }

    /** Confirmação da exclusão da conta. */
    public record ExclusaoContaRequest(
            @NotBlank(message = "Informe sua senha para excluir a conta.")
            @Size(max = 255, message = "Senha atual inválida.")
            String senhaAtual
    ) {
    }
}
