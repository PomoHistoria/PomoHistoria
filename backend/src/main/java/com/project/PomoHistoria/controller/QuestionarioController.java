package com.project.PomoHistoria.controller;

import com.project.PomoHistoria.model.Questionario;
import com.project.PomoHistoria.model.QuestionarioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/questionario")
public class QuestionarioController {

    private final QuestionarioService questionarioService;

    public QuestionarioController(QuestionarioService questionarioService) {
        this.questionarioService = questionarioService;
    }

    @GetMapping("/modulos/{modulo}/partes/{parte}")
    public List<QuestaoResponse> listar(
            @PathVariable int modulo,
            @PathVariable int parte
    ) {
        return questionarioService.listarPorModuloEParte(modulo, parte).stream()
                .map(QuestaoResponse::de)
                .toList();
    }

    @PostMapping
    public ResponseEntity<QuestaoResponse> cadastrar(@Valid @RequestBody QuestaoRequest request) {
        Questionario questao = questionarioService.cadastrar(
                request.modulo(),
                request.parte(),
                request.ordem(),
                request.enunciado(),
                request.alternativaA(),
                request.alternativaB(),
                request.alternativaC(),
                request.alternativaD(),
                request.respostaCorreta()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(QuestaoResponse.de(questao));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> tratarErroDoQuestionario(ResponseStatusException exception) {
        String mensagem = exception.getReason() == null
                ? "Não foi possível concluir a operação."
                : exception.getReason();
        return ResponseEntity.status(exception.getStatusCode())
                .body(new ErrorResponse(mensagem, mensagem));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> tratarErroDeValidacao(MethodArgumentNotValidException exception) {
        FieldError erro = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .orElse(null);
        String mensagem = erro == null ? "Dados inválidos." : erro.getDefaultMessage();
        return ResponseEntity.badRequest().body(new ErrorResponse(mensagem, mensagem));
    }

    public record QuestaoRequest(
            @Min(1) @Max(2) int modulo,
            @Min(1) @Max(3) int parte,
            @Min(1) @Max(5) int ordem,
            @NotBlank @Size(max = 1000) String enunciado,
            @NotBlank @Size(max = 500) String alternativaA,
            @NotBlank @Size(max = 500) String alternativaB,
            @NotBlank @Size(max = 500) String alternativaC,
            @NotBlank @Size(max = 500) String alternativaD,
            @NotBlank @Pattern(regexp = "[ABCD]", message = "A resposta correta deve ser A, B, C ou D.")
            String respostaCorreta
    ) {
    }

    public record QuestaoResponse(
            UUID id,
            int modulo,
            int parte,
            int ordem,
            String enunciado,
            String alternativaA,
            String alternativaB,
            String alternativaC,
            String alternativaD
    ) {

        private static QuestaoResponse de(Questionario questao) {
            return new QuestaoResponse(
                    questao.getId(),
                    questao.getModulo(),
                    questao.getParte(),
                    questao.getOrdem(),
                    questao.getEnunciado(),
                    questao.getAlternativaA(),
                    questao.getAlternativaB(),
                    questao.getAlternativaC(),
                    questao.getAlternativaD()
            );
        }
    }

    public record ErrorResponse(String message, String mensagem) {
    }
}
