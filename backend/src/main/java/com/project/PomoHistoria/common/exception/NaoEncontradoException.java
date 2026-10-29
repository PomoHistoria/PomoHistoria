package com.project.PomoHistoria.common.exception;

/** O registro pedido não existe (HTTP 404). */
public class NaoEncontradoException extends RuntimeException {

    public NaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
