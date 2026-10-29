package com.project.PomoHistoria.common.exception;

/** O usuário não pode executar esta operação agora (HTTP 403). */
public class OperacaoNaoPermitidaException extends RuntimeException {

    public OperacaoNaoPermitidaException(String mensagem) {
        super(mensagem);
    }
}
