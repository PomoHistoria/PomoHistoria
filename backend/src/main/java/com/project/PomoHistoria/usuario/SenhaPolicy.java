package com.project.PomoHistoria.usuario;

import com.project.PomoHistoria.common.exception.DadosInvalidosException;
import java.nio.charset.StandardCharsets;

/** Regras de senha. O BCrypt só aceita até 72 bytes (acentos e emojis ocupam mais de 1 byte). */
public final class SenhaPolicy {

    public static final int TAMANHO_MINIMO = 8;
    public static final int MAXIMO_BYTES = 72;

    private SenhaPolicy() {
    }

    public static boolean excedeLimiteDeBytes(String senha) {
        return senha.getBytes(StandardCharsets.UTF_8).length > MAXIMO_BYTES;
    }

    public static void validarNovaSenha(String senha) {
        if (senha == null || senha.length() < TAMANHO_MINIMO) {
            throw new DadosInvalidosException("A senha deve ter no mínimo " + TAMANHO_MINIMO + " caracteres.");
        }
        if (excedeLimiteDeBytes(senha)) {
            throw new DadosInvalidosException("A senha é longa demais. Use até 72 caracteres sem acentos ou emojis.");
        }
    }
}
