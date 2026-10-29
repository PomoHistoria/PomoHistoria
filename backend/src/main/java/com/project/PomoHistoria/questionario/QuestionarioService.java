package com.project.PomoHistoria.questionario;

import static com.project.PomoHistoria.questionario.QuestionarioRegras.*;

import com.project.PomoHistoria.common.exception.ConflitoException;
import com.project.PomoHistoria.common.exception.DadosInvalidosException;
import com.project.PomoHistoria.common.exception.NaoEncontradoException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuestionarioService {


    private final QuestionarioRepository questionarioRepository;
    private final QuestionarioProgressoService progressoService;

    public QuestionarioService(
            QuestionarioRepository questionarioRepository,
            QuestionarioProgressoService progressoService
    ) {
        this.questionarioRepository = questionarioRepository;
        this.progressoService = progressoService;
    }

    @Transactional(readOnly = true)
    public List<Questionario> listarPorModuloEParte(int modulo, int parte, UUID usuarioId) {
        validarModuloEParte(modulo, parte);
        progressoService.validarEtapaLiberada(modulo, parte, usuarioId);
        return questionarioRepository.findAllByModuloAndParteOrderByOrdemAsc(modulo, parte);
    }

    @Transactional
    public Resultado validarRespostas(
            int modulo,
            int parte,
            UUID usuarioId,
            List<Resposta> respostas
    ) {
        validarModuloEParte(modulo, parte);
        progressoService.validarEtapaDisponivelParaResposta(modulo, parte, usuarioId);

        List<Questionario> questoes = questionarioRepository.findAllByModuloAndParteOrderByOrdemAsc(modulo, parte);
        if (questoes.size() != QUESTOES_POR_PARTE) {
            throw new ConflitoException("A etapa precisa conter exatamente 4 questões antes de ser validada.");
        }
        if (respostas.size() != questoes.size()) {
            throw new DadosInvalidosException("É necessário responder todas as questões.");
        }

        Map<UUID, String> respostasPorQuestao = new HashMap<>();
        for (Resposta resposta : respostas) {
            if (respostasPorQuestao.putIfAbsent(
                    resposta.questaoId(),
                    resposta.alternativa().toUpperCase(Locale.ROOT)
            ) != null) {
                throw new DadosInvalidosException("Há questões respondidas mais de uma vez.");
            }
        }

        int corretas = 0;
        for (Questionario questao : questoes) {
            String alternativa = respostasPorQuestao.remove(questao.getId());
            if (alternativa == null) {
                throw new DadosInvalidosException("As respostas não correspondem às questões desta etapa.");
            }
            if (questao.getRespostaCorreta().equalsIgnoreCase(alternativa)) {
                corretas++;
            }
        }
        if (!respostasPorQuestao.isEmpty()) {
            throw new DadosInvalidosException("As respostas não correspondem às questões desta etapa.");
        }

        boolean aprovado = corretas * 100 >= questoes.size() * APROVACAO_PERCENTUAL;
        Integer proximoModulo = null;
        Integer proximaParte = null;
        if (aprovado) {
            progressoService.registrarConclusao(usuarioId, modulo, parte, corretas, questoes.size());
            if (parte < PARTES_POR_MODULO) {
                proximoModulo = modulo;
                proximaParte = parte + 1;
            } else if (modulo < MODULOS) {
                proximoModulo = modulo + 1;
                proximaParte = 1;
            }
        }

        return new Resultado(
                aprovado,
                corretas,
                questoes.size(),
                percentual(corretas, questoes.size()),
                APROVACAO_PERCENTUAL,
                proximoModulo,
                proximaParte
        );
    }

    /** Todas as questões, na ordem do questionário (uso do admin). */
    @Transactional(readOnly = true)
    public List<Questionario> listarTodas() {
        return questionarioRepository.findAllByOrderByModuloAscParteAscOrdemAsc();
    }

    /** Edita o texto e o gabarito de uma questão. Módulo, parte e ordem não mudam. */
    @Transactional
    public Questionario editar(
            UUID id,
            String enunciado,
            String alternativaA,
            String alternativaB,
            String alternativaC,
            String alternativaD,
            String alternativaE,
            String respostaCorreta
    ) {
        Questionario questao = questionarioRepository.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("Questão não encontrada."));

        questao.setEnunciado(enunciado.trim());
        questao.setAlternativaA(alternativaA.trim());
        questao.setAlternativaB(alternativaB.trim());
        questao.setAlternativaC(alternativaC.trim());
        questao.setAlternativaD(alternativaD.trim());
        questao.setAlternativaE(alternativaE.trim());
        questao.setRespostaCorreta(respostaCorreta.trim().toUpperCase(Locale.ROOT));

        return questionarioRepository.save(questao);
    }

    private int percentual(int corretas, int total) {
        return Math.round((float) corretas * 100 / total);
    }

    private void validarModuloEParte(int modulo, int parte) {
        if (modulo < 1 || modulo > MODULOS) {
            throw new DadosInvalidosException("O módulo deve ser 1 ou 2.");
        }
        if (parte < 1 || parte > PARTES_POR_MODULO) {
            throw new DadosInvalidosException("A parte deve estar entre 1 e 3.");
        }
    }

    public record Resposta(UUID questaoId, String alternativa) {
    }

    public record Resultado(
            boolean aprovado,
            int respostasCorretas,
            int totalQuestoes,
            int percentual,
            int percentualMinimo,
            Integer proximoModulo,
            Integer proximaParte
    ) {
    }

}
