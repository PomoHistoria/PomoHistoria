package com.project.PomoHistoria.common;

import com.project.PomoHistoria.common.exception.ConflitoException;
import com.project.PomoHistoria.common.exception.CredenciaisInvalidasException;
import com.project.PomoHistoria.common.exception.DadosInvalidosException;
import com.project.PomoHistoria.common.exception.MuitasTentativasException;
import com.project.PomoHistoria.common.exception.NaoEncontradoException;
import com.project.PomoHistoria.common.exception.OperacaoNaoPermitidaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DadosInvalidosException.class)
    public ResponseEntity<ErrorResponse> dadosInvalidos(DadosInvalidosException exception) {
        return resposta(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ErrorResponse> credenciaisInvalidas(CredenciaisInvalidasException exception) {
        return resposta(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }

    @ExceptionHandler(OperacaoNaoPermitidaException.class)
    public ResponseEntity<ErrorResponse> operacaoNaoPermitida(OperacaoNaoPermitidaException exception) {
        return resposta(HttpStatus.FORBIDDEN, exception.getMessage());
    }

    @ExceptionHandler(NaoEncontradoException.class)
    public ResponseEntity<ErrorResponse> naoEncontrado(NaoEncontradoException exception) {
        return resposta(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ErrorResponse> conflito(ConflitoException exception) {
        return resposta(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(MuitasTentativasException.class)
    public ResponseEntity<ErrorResponse> muitasTentativas(MuitasTentativasException exception) {
        return resposta(HttpStatus.TOO_MANY_REQUESTS, exception.getMessage());
    }

    /** Duas requisições simultâneas gravando o mesmo registro único (ex.: clique duplo). */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> violacaoDeIntegridade(DataIntegrityViolationException exception) {
        log.info("Violação de integridade tratada como conflito: {}", exception.getMostSpecificCause().getMessage());
        return resposta(HttpStatus.CONFLICT, "A operação conflita com um registro já existente.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacao(MethodArgumentNotValidException exception) {
        FieldError erro = exception.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        String mensagem = erro == null || erro.getDefaultMessage() == null ? "Dados inválidos." : erro.getDefaultMessage();
        return resposta(HttpStatus.BAD_REQUEST, mensagem);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> requisicaoMalFormada(Exception exception) {
        return resposta(HttpStatus.BAD_REQUEST, "Requisição inválida.");
    }

    /** Erros inesperados: registra no log e não expõe detalhes internos ao cliente. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> erroInesperado(Exception exception) throws Exception {
        // Deixa o Spring Security responder 401/403 normalmente.
        if (exception instanceof AccessDeniedException || exception instanceof AuthenticationException) {
            throw exception;
        }
        // Erros do próprio Spring MVC (404, 405, 415...) mantêm o status correto.
        if (exception instanceof org.springframework.web.ErrorResponse erroSpring) {
            HttpStatusCode status = erroSpring.getStatusCode();
            return ResponseEntity.status(status).body(new ErrorResponse(mensagemPadrao(status)));
        }
        log.error("Erro inesperado ao processar a requisição", exception);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno. Tente novamente mais tarde.");
    }

    private static String mensagemPadrao(HttpStatusCode status) {
        return switch (status.value()) {
            case 404 -> "Recurso não encontrado.";
            case 405 -> "Método não permitido.";
            case 415 -> "Tipo de conteúdo não suportado.";
            default -> status.is4xxClientError() ? "Requisição inválida." : "Erro interno. Tente novamente mais tarde.";
        };
    }

    private static ResponseEntity<ErrorResponse> resposta(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(new ErrorResponse(mensagem));
    }
}
