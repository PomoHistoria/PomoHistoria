package com.project.PomoHistoria.questionario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;

/** Primeira vez que o usuário abriu um módulo do questionário. */
@Entity
@Table(
        name = "modulo_iniciado",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_modulo_iniciado_usuario_modulo",
                columnNames = {"usuario_id", "modulo"}
        )
)
public class ModuloIniciado {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "usuario_id", nullable = false, updatable = false)
    private UUID usuarioId;

    @Column(nullable = false, updatable = false)
    private int modulo;

    @Column(name = "iniciado_em", nullable = false, updatable = false)
    private Instant iniciadoEm;

    protected ModuloIniciado() {
    }

    public ModuloIniciado(UUID usuarioId, int modulo) {
        this.usuarioId = usuarioId;
        this.modulo = modulo;
        this.iniciadoEm = Instant.now();
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public int getModulo() {
        return modulo;
    }

    public Instant getIniciadoEm() {
        return iniciadoEm;
    }
}
