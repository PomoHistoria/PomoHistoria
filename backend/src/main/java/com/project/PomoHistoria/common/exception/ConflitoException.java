package com.project.PomoHistoria.common.exception;

/** Dados conflitam com um registro existente (HTTP 409). */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
