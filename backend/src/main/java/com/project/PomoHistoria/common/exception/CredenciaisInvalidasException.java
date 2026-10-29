package com.project.PomoHistoria.common.exception;

/** Email ou senha incorretos, ou conta inativa (HTTP 401). */
public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException(String mensagem) {
        super(mensagem);
    }
}
