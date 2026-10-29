package com.project.PomoHistoria.questionario;

import static com.project.PomoHistoria.questionario.QuestionarioRegras.*;

import com.project.PomoHistoria.common.exception.ConflitoException;
import com.project.PomoHistoria.common.exception.DadosInvalidosException;
import com.project.PomoHistoria.common.exception.OperacaoNaoPermitidaException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuestionarioProgressoService {

    private final QuestionarioProgressoRepository progressoRepository;
    private final ModuloIniciadoRepository moduloIniciadoRepository;

    public QuestionarioProgressoService(
            QuestionarioProgressoRepository progressoRepository,
            ModuloIniciadoRepository moduloIniciadoRepository
    ) {
        this.progressoRepository = progressoRepository;
        this.moduloIniciadoRepository = moduloIniciadoRepository;
    }

    @Transactional(readOnly = true)
    public Progresso obterProgresso(UUID usuarioId) {
        Map<Etapa, QuestionarioProgresso> progresso = carregarProgresso(usuarioId);
        List<EtapaProgresso> etapas = new ArrayList<>();
        Integer moduloAtual = null;
        Integer parteAtual = null;
        int modulosConcluidos = 0;

        for (int modulo = 1; modulo <= MODULOS; modulo++) {
            boolean moduloConcluido = true;
            for (int parte = 1; parte <= PARTES_POR_MODULO; parte++) {
                Etapa chave = new Etapa(modulo, parte);
                QuestionarioProgresso registro = progresso.get(chave);
                boolean concluida = registro != null;
                boolean liberada = estaLiberada(modulo, parte, progresso);
                if (!concluida) {
                    moduloConcluido = false;
                    if (liberada && moduloAtual == null) {
                        moduloAtual = modulo;
                        parteAtual = parte;
                    }
                }
                etapas.add(new EtapaProgresso(
                        modulo,
                        parte,
                        liberada,
                        concluida,
                        registro == null ? 0 : registro.getRespostasCorretas(),
                        registro == null ? QUESTOES_POR_PARTE : registro.getTotalQuestoes(),
                        registro == null ? 0 : percentual(registro.getRespostasCorretas(), registro.getTotalQuestoes())
                ));
            }
            if (moduloConcluido) {
                modulosConcluidos++;
            }
        }

        List<Integer> modulosIniciados = moduloIniciadoRepository.findAllByUsuarioIdOrderByModuloAsc(usuarioId).stream()
                .map(ModuloIniciado::getModulo)
                .toList();
        return new Progresso(etapas, modulosConcluidos, moduloAtual, parteAtual, moduloAtual == null, modulosIniciados);
    }

    /**
     * Marca que o usuário abriu o módulo (só a primeira vez conta). O módulo precisa estar liberado.
     * Sem @Transactional: cada gravação roda na própria transação, e o "já existe" de um clique duplo é ignorado.
     */
    public void iniciarModulo(UUID usuarioId, int modulo) {
        if (modulo < 1 || modulo > MODULOS) {
            throw new DadosInvalidosException("Módulo inválido.");
        }
        if (!estaLiberada(modulo, 1, carregarProgresso(usuarioId))) {
            throw new OperacaoNaoPermitidaException("Este módulo ainda está bloqueado.");
        }
        if (moduloIniciadoRepository.existsByUsuarioIdAndModulo(usuarioId, modulo)) {
            return;
        }
        try {
            moduloIniciadoRepository.saveAndFlush(new ModuloIniciado(usuarioId, modulo));
        } catch (DataIntegrityViolationException exception) {
            // Outra requisição simultânea já registrou: resultado é o mesmo.
        }
    }

    @Transactional(readOnly = true)
    public void validarEtapaLiberada(int modulo, int parte, UUID usuarioId) {
        Map<Etapa, QuestionarioProgresso> progresso = carregarProgresso(usuarioId);
        if (!estaLiberada(modulo, parte, progresso)) {
            throw new OperacaoNaoPermitidaException("Esta etapa ainda está bloqueada.");
        }
    }

    @Transactional(readOnly = true)
    public void validarEtapaDisponivelParaResposta(int modulo, int parte, UUID usuarioId) {
        Map<Etapa, QuestionarioProgresso> progresso = carregarProgresso(usuarioId);
        Etapa etapa = new Etapa(modulo, parte);
        if (progresso.containsKey(etapa)) {
            throw new ConflitoException("Esta etapa já foi concluída.");
        }
        if (!estaLiberada(modulo, parte, progresso)) {
            throw new OperacaoNaoPermitidaException("Esta etapa ainda está bloqueada.");
        }
    }

    @Transactional
    public void registrarConclusao(UUID usuarioId, int modulo, int parte, int respostasCorretas, int totalQuestoes) {
        progressoRepository.save(new QuestionarioProgresso(
                usuarioId,
                modulo,
                parte,
                respostasCorretas,
                totalQuestoes
        ));
    }

    private Map<Etapa, QuestionarioProgresso> carregarProgresso(UUID usuarioId) {
        Map<Etapa, QuestionarioProgresso> progresso = new HashMap<>();
        for (QuestionarioProgresso registro : progressoRepository.findAllByUsuarioIdOrderByModuloAscParteAsc(usuarioId)) {
            progresso.put(new Etapa(registro.getModulo(), registro.getParte()), registro);
        }
        return progresso;
    }

    private boolean estaLiberada(int modulo, int parte, Map<Etapa, QuestionarioProgresso> progresso) {
        if (modulo == 1 && parte == 1) {
            return true;
        }
        int moduloAnterior = parte == 1 ? modulo - 1 : modulo;
        int parteAnterior = parte == 1 ? PARTES_POR_MODULO : parte - 1;
        return progresso.containsKey(new Etapa(moduloAnterior, parteAnterior));
    }

    private int percentual(int corretas, int total) {
        return Math.round((float) corretas * 100 / total);
    }

    public record EtapaProgresso(
            int modulo,
            int parte,
            boolean liberada,
            boolean concluida,
            int respostasCorretas,
            int totalQuestoes,
            int percentual
    ) {
    }

    public record Progresso(
            List<EtapaProgresso> etapas,
            int modulosConcluidos,
            Integer moduloAtual,
            Integer parteAtual,
            boolean concluido,
            /** Módulos que o usuário já abriu pelo menos uma vez (define o fundo da tela). */
            List<Integer> modulosIniciados
    ) {
    }

    private record Etapa(int modulo, int parte) {
    }
}
