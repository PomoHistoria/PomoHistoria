package com.project.PomoHistoria.questionario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(
        name = "questoes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_questoes_modulo_parte_ordem",
                columnNames = {"modulo", "parte", "ordem"}
        )
)
public class Questionario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private int modulo;

    @Column(nullable = false)
    private int parte;

    @Column(nullable = false)
    private int ordem;

    @Column(nullable = false, length = 1000)
    private String enunciado;

    @Column(name = "alternativa_a", nullable = false, length = 500)
    private String alternativaA;

    @Column(name = "alternativa_b", nullable = false, length = 500)
    private String alternativaB;

    @Column(name = "alternativa_c", nullable = false, length = 500)
    private String alternativaC;

    @Column(name = "alternativa_d", nullable = false, length = 500)
    private String alternativaD;

    @Column(name = "alternativa_e", nullable = false, length = 500)
    private String alternativaE;

    // A migração V1 cria a coluna como CHAR(1).
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "resposta_correta", nullable = false, length = 1)
    private String respostaCorreta;

    public Questionario() {
    }

    public UUID getId() {
        return id;
    }

    public int getModulo() {
        return modulo;
    }

    public void setModulo(int modulo) {
        this.modulo = modulo;
    }

    public int getParte() {
        return parte;
    }

    public void setParte(int parte) {
        this.parte = parte;
    }

    public int getOrdem() {
        return ordem;
    }

    public void setOrdem(int ordem) {
        this.ordem = ordem;
    }

    public String getEnunciado() {
        return enunciado;
    }

    public void setEnunciado(String enunciado) {
        this.enunciado = enunciado;
    }

    public String getAlternativaA() {
        return alternativaA;
    }

    public void setAlternativaA(String alternativaA) {
        this.alternativaA = alternativaA;
    }

    public String getAlternativaB() {
        return alternativaB;
    }

    public void setAlternativaB(String alternativaB) {
        this.alternativaB = alternativaB;
    }

    public String getAlternativaC() {
        return alternativaC;
    }

    public void setAlternativaC(String alternativaC) {
        this.alternativaC = alternativaC;
    }

    public String getAlternativaD() {
        return alternativaD;
    }

    public void setAlternativaD(String alternativaD) {
        this.alternativaD = alternativaD;
    }

    public String getAlternativaE() {
        return alternativaE;
    }

    public void setAlternativaE(String alternativaE) {
        this.alternativaE = alternativaE;
    }

    public String getRespostaCorreta() {
        return respostaCorreta;
    }

    public void setRespostaCorreta(String respostaCorreta) {
        this.respostaCorreta = respostaCorreta;
    }
}
