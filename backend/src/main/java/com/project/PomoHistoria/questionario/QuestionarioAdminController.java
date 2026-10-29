package com.project.PomoHistoria.questionario;

import com.project.PomoHistoria.questionario.dto.QuestionarioDtos.QuestaoAdminResponse;
import com.project.PomoHistoria.questionario.dto.QuestionarioDtos.QuestaoEdicaoRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Edição das questões. Somente ADMIN: a regra de /api/admin/** está em SecurityConfig. */
@RestController
@RequestMapping("/api/admin/questoes")
public class QuestionarioAdminController {

    private final QuestionarioService questionarioService;

    public QuestionarioAdminController(QuestionarioService questionarioService) {
        this.questionarioService = questionarioService;
    }

    @GetMapping
    public List<QuestaoAdminResponse> listar() {
        return questionarioService.listarTodas().stream()
                .map(QuestaoAdminResponse::de)
                .toList();
    }

    @PutMapping("/{id}")
    public QuestaoAdminResponse editar(@PathVariable UUID id, @Valid @RequestBody QuestaoEdicaoRequest request) {
        return QuestaoAdminResponse.de(questionarioService.editar(
                id,
                request.enunciado(),
                request.alternativaA(),
                request.alternativaB(),
                request.alternativaC(),
                request.alternativaD(),
                request.alternativaE(),
                request.respostaCorreta()
        ));
    }
}
