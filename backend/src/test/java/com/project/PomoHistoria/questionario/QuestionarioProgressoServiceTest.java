package com.project.PomoHistoria.questionario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QuestionarioProgressoServiceTest {

    private static final UUID USUARIO_ID = UUID.randomUUID();

    private QuestionarioProgressoRepository progressoRepository;
    private QuestionarioProgressoService service;
    private List<QuestionarioProgresso> registros;

    @BeforeEach
    void configurar() {
        progressoRepository = mock(QuestionarioProgressoRepository.class);
        service = new QuestionarioProgressoService(progressoRepository, mock(ModuloIniciadoRepository.class));
        registros = new ArrayList<>();

        when(progressoRepository.findAllByUsuarioIdOrderByModuloAscParteAsc(USUARIO_ID))
                .thenAnswer(invocacao -> List.copyOf(registros));
        when(progressoRepository.save(any(QuestionarioProgresso.class)))
                .thenAnswer(invocacao -> {
                    QuestionarioProgresso registro = invocacao.getArgument(0);
                    registros.add(registro);
                    return registro;
                });
    }

    @Test
    void primeiroAcessoLiberaSomentePrimeiraParteDoPrimeiroModulo() {
        QuestionarioProgressoService.Progresso progresso = service.obterProgresso(USUARIO_ID);

        assertEquals(6, progresso.etapas().size());
        assertEquals(1, progresso.moduloAtual());
        assertEquals(1, progresso.parteAtual());
        assertTrue(progresso.etapas().get(0).liberada());
        assertFalse(progresso.etapas().get(1).liberada());
        assertFalse(progresso.etapas().get(3).liberada());
    }

    @Test
    void conclusaoLiberaSomenteEtapaSeguinte() {
        service.registrarConclusao(USUARIO_ID, 1, 1, 3, 4);

        QuestionarioProgressoService.Progresso progresso = service.obterProgresso(USUARIO_ID);

        assertTrue(progresso.etapas().get(0).concluida());
        assertTrue(progresso.etapas().get(1).liberada());
        assertFalse(progresso.etapas().get(2).liberada());
        assertEquals(1, progresso.moduloAtual());
        assertEquals(2, progresso.parteAtual());
    }
}
