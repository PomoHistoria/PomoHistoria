package com.project.PomoHistoria.common.exception;

/** Limite de tentativas excedido (HTTP 429). */
public class MuitasTentativasException extends RuntimeException {

    public MuitasTentativasException(String mensagem) {
        super(mensagem);
    }
}
