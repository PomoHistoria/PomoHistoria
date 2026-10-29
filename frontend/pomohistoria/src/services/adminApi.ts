import type { Alternativa, Questao } from './questionarioApi';
import { apiRequest, ApiError, isRecord } from './http';

/** Questão com gabarito: só disponível para ADMIN. */
export interface QuestaoAdmin extends Questao {
  respostaCorreta: Alternativa;
}

export type QuestaoEdicao = Pick<
  QuestaoAdmin,
  'enunciado' | 'alternativaA' | 'alternativaB' | 'alternativaC' | 'alternativaD' | 'alternativaE' | 'respostaCorreta'
>;

const ALTERNATIVAS = ['A', 'B', 'C', 'D', 'E'];

function isQuestaoAdmin(value: unknown): value is QuestaoAdmin {
  return (
    isRecord(value) &&
    typeof value.id === 'string' &&
    typeof value.modulo === 'number' &&
    typeof value.parte === 'number' &&
    typeof value.ordem === 'number' &&
    typeof value.enunciado === 'string' &&
    typeof value.alternativaA === 'string' &&
    typeof value.alternativaB === 'string' &&
    typeof value.alternativaC === 'string' &&
    typeof value.alternativaD === 'string' &&
    typeof value.alternativaE === 'string' &&
    typeof value.respostaCorreta === 'string' &&
    ALTERNATIVAS.includes(value.respostaCorreta)
  );
}

export const adminApi = {
  async listarQuestoes(): Promise<QuestaoAdmin[]> {
    const data = await apiRequest('/api/admin/questoes');
    if (!Array.isArray(data) || !data.every(isQuestaoAdmin)) {
      throw new ApiError('O servidor retornou as questões em um formato inválido.', 200);
    }
    return data;
  },

  async editarQuestao(id: string, dados: QuestaoEdicao): Promise<QuestaoAdmin> {
    const data = await apiRequest(`/api/admin/questoes/${encodeURIComponent(id)}`, {
      method: 'PUT',
      body: dados,
    });
    if (!isQuestaoAdmin(data)) {
      throw new ApiError('O servidor retornou a questão em um formato inválido.', 200);
    }
    return data;
  },
};
