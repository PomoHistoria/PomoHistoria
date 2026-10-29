package com.project.PomoHistoria.questionario.dto;

import com.project.PomoHistoria.questionario.Questionario;
import com.project.PomoHistoria.questionario.QuestionarioRegras;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

/** Formatos de entrada e saída dos endpoints de /api/questionario. */
public final class QuestionarioDtos {

    private QuestionarioDtos() {
    }

    /** Edição de questão pelo admin. Módulo, parte e ordem não mudam. */
    public record QuestaoEdicaoRequest(
            @NotBlank(message = "O enunciado é obrigatório.")
            @Size(max = 1000, message = "O enunciado deve ter no máximo 1000 caracteres.")
            String enunciado,

            @NotBlank(message = "A alternativa A é obrigatória.")
            @Size(max = 500, message = "Cada alternativa deve ter no máximo 500 caracteres.")
            String alternativaA,

            @NotBlank(message = "A alternativa B é obrigatória.")
            @Size(max = 500, message = "Cada alternativa deve ter no máximo 500 caracteres.")
            String alternativaB,

            @NotBlank(message = "A alternativa C é obrigatória.")
            @Size(max = 500, message = "Cada alternativa deve ter no máximo 500 caracteres.")
            String alternativaC,

            @NotBlank(message = "A alternativa D é obrigatória.")
            @Size(max = 500, message = "Cada alternativa deve ter no máximo 500 caracteres.")
            String alternativaD,

            @NotBlank(message = "A alternativa E é obrigatória.")
            @Size(max = 500, message = "Cada alternativa deve ter no máximo 500 caracteres.")
            String alternativaE,

            @NotBlank(message = "A resposta correta é obrigatória.")
            @Pattern(regexp = "[ABCDE]", message = "A resposta correta deve ser A, B, C, D ou E.")
            String respostaCorreta
    ) {
    }

    /** Questão completa, com gabarito. Só sai pelas rotas de /api/admin. */
    public record QuestaoAdminResponse(
            UUID id,
            int modulo,
            int parte,
            int ordem,
            String enunciado,
            String alternativaA,
            String alternativaB,
            String alternativaC,
            String alternativaD,
            String alternativaE,
            String respostaCorreta
    ) {

        public static QuestaoAdminResponse de(Questionario questao) {
            return new QuestaoAdminResponse(
                    questao.getId(),
                    questao.getModulo(),
                    questao.getParte(),
                    questao.getOrdem(),
                    questao.getEnunciado(),
                    questao.getAlternativaA(),
                    questao.getAlternativaB(),
                    questao.getAlternativaC(),
                    questao.getAlternativaD(),
                    questao.getAlternativaE(),
                    questao.getRespostaCorreta()
            );
        }
    }

    public record ValidacaoRequest(
            @NotNull(message = "As respostas são obrigatórias.")
            @Size(
                    min = QuestionarioRegras.QUESTOES_POR_PARTE,
                    max = QuestionarioRegras.QUESTOES_POR_PARTE,
                    message = "É necessário responder todas as questões."
            )
            List<@NotNull @Valid RespostaRequest> respostas
    ) {
    }

    public record RespostaRequest(
            @NotNull(message = "Questão inválida.")
            UUID questaoId,

            @NotBlank(message = "A alternativa é obrigatória.")
            @Pattern(regexp = "[ABCDE]", message = "A alternativa deve ser A, B, C, D ou E.")
            String alternativa
    ) {
    }

    /** Não inclui a resposta correta, que nunca sai do servidor. */
    public record QuestaoResponse(
            UUID id,
            int modulo,
            int parte,
            int ordem,
            String enunciado,
            String alternativaA,
            String alternativaB,
            String alternativaC,
            String alternativaD,
            String alternativaE
    ) {

        public static QuestaoResponse de(Questionario questao) {
            return new QuestaoResponse(
                    questao.getId(),
                    questao.getModulo(),
                    questao.getParte(),
                    questao.getOrdem(),
                    questao.getEnunciado(),
                    questao.getAlternativaA(),
                    questao.getAlternativaB(),
                    questao.getAlternativaC(),
                    questao.getAlternativaD(),
                    questao.getAlternativaE()
            );
        }
    }
}
