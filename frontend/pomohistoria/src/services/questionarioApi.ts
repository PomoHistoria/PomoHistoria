import { apiRequest, ApiError, isRecord } from './http';

export type Alternativa = 'A' | 'B' | 'C' | 'D' | 'E';

export interface Questao {
  id: string;
  modulo: number;
  parte: number;
  ordem: number;
  enunciado: string;
  alternativaA: string;
  alternativaB: string;
  alternativaC: string;
  alternativaD: string;
  alternativaE: string;
}

export interface EtapaProgresso {
  modulo: number;
  parte: number;
  liberada: boolean;
  concluida: boolean;
  respostasCorretas: number;
  totalQuestoes: number;
  percentual: number;
}

export interface ProgressoQuestionario {
  etapas: EtapaProgresso[];
  modulosConcluidos: number;
  moduloAtual: number | null;
  parteAtual: number | null;
  concluido: boolean;
  /** Módulos que o usuário já abriu pelo menos uma vez (define o fundo da tela). */
  modulosIniciados: number[];
}

export interface ResultadoValidacao {
  aprovado: boolean;
  respostasCorretas: number;
  totalQuestoes: number;
  percentual: number;
  percentualMinimo: number;
  proximoModulo: number | null;
  proximaParte: number | null;
}

export interface RespostaQuestao {
  questaoId: string;
  alternativa: Alternativa;
}

function numeroOuNulo(value: unknown): value is number | null {
  return typeof value === 'number' || value === null;
}

function isEtapaProgresso(value: unknown): value is EtapaProgresso {
  return (
    isRecord(value) &&
    typeof value.modulo === 'number' &&
    typeof value.parte === 'number' &&
    typeof value.liberada === 'boolean' &&
    typeof value.concluida === 'boolean' &&
    typeof value.respostasCorretas === 'number' &&
    typeof value.totalQuestoes === 'number' &&
    typeof value.percentual === 'number'
  );
}

function isQuestao(value: unknown): value is Questao {
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
    typeof value.alternativaE === 'string'
  );
}

function formatoInvalido(descricao: string): never {
  throw new ApiError(`O servidor retornou ${descricao} em um formato inválido.`, 200);
}

export async function listarQuestoes(modulo: number, parte: number): Promise<Questao[]> {
  const data = await apiRequest(`/api/questionario/modulos/${modulo}/partes/${parte}`);
  if (!Array.isArray(data) || !data.every(isQuestao)) formatoInvalido('questões');
  return [...data].sort((a, b) => a.ordem - b.ordem);
}

export async function obterProgresso(): Promise<ProgressoQuestionario> {
  const data = await apiRequest('/api/questionario/progresso');
  if (
    !isRecord(data) ||
    !Array.isArray(data.etapas) ||
    !data.etapas.every(isEtapaProgresso) ||
    typeof data.modulosConcluidos !== 'number' ||
    !numeroOuNulo(data.moduloAtual) ||
    !numeroOuNulo(data.parteAtual) ||
    typeof data.concluido !== 'boolean' ||
    !(data.modulosIniciados === undefined ||
      (Array.isArray(data.modulosIniciados) && data.modulosIniciados.every((m) => typeof m === 'number')))
  ) {
    formatoInvalido('um progresso');
  }

  return {
    etapas: data.etapas,
    modulosConcluidos: data.modulosConcluidos,
    moduloAtual: data.moduloAtual,
    parteAtual: data.parteAtual,
    concluido: data.concluido,
    modulosIniciados: (data.modulosIniciados as number[] | undefined) ?? [],
  };
}

/** Registra que o usuário abriu o módulo (só a primeira vez conta no servidor). */
export async function iniciarModulo(modulo: number): Promise<void> {
  await apiRequest(`/api/questionario/modulos/${modulo}/iniciar`, { method: 'POST' });
}

export async function validarEtapa(
  modulo: number,
  parte: number,
  respostas: RespostaQuestao[]
): Promise<ResultadoValidacao> {
  const data = await apiRequest(`/api/questionario/modulos/${modulo}/partes/${parte}/validar`, {
    method: 'POST',
    body: { respostas },
  });
  if (
    !isRecord(data) ||
    typeof data.aprovado !== 'boolean' ||
    typeof data.respostasCorretas !== 'number' ||
    typeof data.totalQuestoes !== 'number' ||
    typeof data.percentual !== 'number' ||
    typeof data.percentualMinimo !== 'number' ||
    !numeroOuNulo(data.proximoModulo) ||
    !numeroOuNulo(data.proximaParte)
  ) {
    formatoInvalido('um resultado');
  }

  return {
    aprovado: data.aprovado,
    respostasCorretas: data.respostasCorretas,
    totalQuestoes: data.totalQuestoes,
    percentual: data.percentual,
    percentualMinimo: data.percentualMinimo,
    proximoModulo: data.proximoModulo,
    proximaParte: data.proximaParte,
  };
}
