package com.project.PomoHistoria.questionario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "progresso_questionario",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_progresso_usuario_modulo_parte",
                columnNames = {"usuario_id", "modulo", "parte"}
        )
)
public class QuestionarioProgresso {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "usuario_id", nullable = false, updatable = false)
    private UUID usuarioId;

    @Column(nullable = false)
    private int modulo;

    @Column(nullable = false)
    private int parte;

    @Column(name = "respostas_corretas", nullable = false)
    private int respostasCorretas;

    @Column(name = "total_questoes", nullable = false)
    private int totalQuestoes;

    @Column(name = "concluida_em", nullable = false, updatable = false)
    private Instant concluidaEm;

    protected QuestionarioProgresso() {
    }

    public QuestionarioProgresso(
            UUID usuarioId,
            int modulo,
            int parte,
            int respostasCorretas,
            int totalQuestoes
    ) {
        this.usuarioId = usuarioId;
        this.modulo = modulo;
        this.parte = parte;
        this.respostasCorretas = respostasCorretas;
        this.totalQuestoes = totalQuestoes;
    }

    @PrePersist
    void prepararCriacao() {
        if (concluidaEm == null) {
            concluidaEm = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public int getModulo() {
        return modulo;
    }

    public int getParte() {
        return parte;
    }

    public int getRespostasCorretas() {
        return respostasCorretas;
    }

    public int getTotalQuestoes() {
        return totalQuestoes;
    }

    public Instant getConcluidaEm() {
        return concluidaEm;
    }
}
