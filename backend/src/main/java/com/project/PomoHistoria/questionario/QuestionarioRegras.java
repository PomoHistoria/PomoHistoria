package com.project.PomoHistoria.questionario;

/** Estrutura fixa do questionário. Mudanças aqui exigem revisar os CHECKs das migrações do banco. */
public final class QuestionarioRegras {

    public static final int MODULOS = 2;
    public static final int PARTES_POR_MODULO = 3;
    public static final int QUESTOES_POR_PARTE = 4;
    public static final int APROVACAO_PERCENTUAL = 70;

    private QuestionarioRegras() {
    }
}
