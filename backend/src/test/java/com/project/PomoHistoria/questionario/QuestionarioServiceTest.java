package com.project.PomoHistoria.questionario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.project.PomoHistoria.common.exception.OperacaoNaoPermitidaException;

class QuestionarioServiceTest {

    private static final UUID USUARIO_ID = UUID.randomUUID();
    private static final List<String> GABARITO = List.of("A", "B", "E", "D");

    private QuestionarioRepository questionarioRepository;
    private QuestionarioProgressoService progressoService;
    private QuestionarioService service;
    private List<Questionario> questoes;

    @BeforeEach
    void configurar() {
        questionarioRepository = mock(QuestionarioRepository.class);
        progressoService = mock(QuestionarioProgressoService.class);
        service = new QuestionarioService(questionarioRepository, progressoService);
        questoes = criarQuestoes();

        when(questionarioRepository.findAllByModuloAndParteOrderByOrdemAsc(1, 1)).thenReturn(questoes);
        when(questionarioRepository.findAllByModuloAndParteOrderByOrdemAsc(1, 2)).thenReturn(questoes);
        when(progressoService.obterProgresso(USUARIO_ID)).thenReturn(
                new QuestionarioProgressoService.Progresso(
                        List.of(
                                new QuestionarioProgressoService.EtapaProgresso(1, 1, true, true, 3, 4, 75),
                                new QuestionarioProgressoService.EtapaProgresso(1, 2, true, false, 0, 4, 0)
                        ),
                        0,
                        1,
                        2,
                        false,
                        List.of(1)
                )
        );
    }

    @Test
    void aprovaComTresAcertosERegistraProximaEtapa() {
        QuestionarioService.Resultado resultado = service.validarRespostas(
                1,
                1,
                USUARIO_ID,
                respostasComAcertos(3)
        );

        assertTrue(resultado.aprovado());
        assertEquals(3, resultado.respostasCorretas());
        assertEquals(75, resultado.percentual());
        assertEquals(1, resultado.proximoModulo());
        assertEquals(2, resultado.proximaParte());
        verify(progressoService).registrarConclusao(USUARIO_ID, 1, 1, 3, 4);
    }

    @Test
    void naoAprovaComDoisAcertosNemSalvaProgresso() {
        QuestionarioService.Resultado resultado = service.validarRespostas(
                1,
                1,
                USUARIO_ID,
                respostasComAcertos(2)
        );

        assertFalse(resultado.aprovado());
        assertEquals(50, resultado.percentual());
        verify(progressoService, never()).registrarConclusao(
                eq(USUARIO_ID),
                anyInt(),
                anyInt(),
                anyInt(),
                anyInt()
        );
    }

    @Test
    void bloqueiaTentativaEmEtapaFutura() {
        doThrow(new OperacaoNaoPermitidaException("Esta etapa ainda está bloqueada."))
                .when(progressoService)
                .validarEtapaDisponivelParaResposta(2, 1, USUARIO_ID);

        assertThrows(
                OperacaoNaoPermitidaException.class,
                () -> service.validarRespostas(2, 1, USUARIO_ID, respostasComAcertos(4))
        );
        verify(progressoService).validarEtapaDisponivelParaResposta(2, 1, USUARIO_ID);
        verify(questionarioRepository, never()).findAllByModuloAndParteOrderByOrdemAsc(2, 1);
    }

    private List<Questionario> criarQuestoes() {
        List<Questionario> lista = new ArrayList<>();
        for (int ordem = 0; ordem < GABARITO.size(); ordem++) {
            Questionario questao = mock(Questionario.class);
            when(questao.getId()).thenReturn(UUID.randomUUID());
            when(questao.getRespostaCorreta()).thenReturn(GABARITO.get(ordem));
            lista.add(questao);
        }
        return lista;
    }

    private List<QuestionarioService.Resposta> respostasComAcertos(int acertos) {
        List<QuestionarioService.Resposta> respostas = new ArrayList<>();
        for (int ordem = 0; ordem < questoes.size(); ordem++) {
            String alternativa = ordem < acertos ? GABARITO.get(ordem) : alternativaErrada(GABARITO.get(ordem));
            respostas.add(new QuestionarioService.Resposta(questoes.get(ordem).getId(), alternativa));
        }
        return respostas;
    }

    private String alternativaErrada(String correta) {
        return "A".equals(correta) ? "B" : "A";
    }
}
