package com.project.PomoHistoria.common.exception;

/** Dados enviados não atendem às regras (HTTP 400). */
public class DadosInvalidosException extends RuntimeException {

    public DadosInvalidosException(String mensagem) {
        super(mensagem);
    }
}
