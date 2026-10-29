/** Cliente HTTP compartilhado: URL da API, sessão por cookie, token CSRF e erros padronizados. */

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? '').trim().replace(/\/+$/, '');

const MENSAGEM_SEM_CONFIGURACAO =
  'O site não está configurado com o endereço do servidor (VITE_API_BASE_URL).';

if (!API_BASE_URL) {
  console.error(`${MENSAGEM_SEM_CONFIGURACAO} Defina a variável e gere um novo build.`);
}

/** Erro de API com o status HTTP (0 quando o servidor não respondeu). */
export class ApiError extends Error {
  readonly status: number;

  constructor(message: string, status: number) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
  }

  get naoAutenticado(): boolean {
    return this.status === 401;
  }
}

type Metodo = 'GET' | 'POST' | 'PUT' | 'DELETE';

// Avisados quando o servidor diz que a sessão não vale mais (401), para a tela voltar ao login.
const ouvintesDeSessaoExpirada = new Set<() => void>();

/** Registra uma função chamada a cada 401 da API. Devolve a função que cancela o registro. */
export function aoExpirarSessao(ouvinte: () => void): () => void {
  ouvintesDeSessaoExpirada.add(ouvinte);
  return () => ouvintesDeSessaoExpirada.delete(ouvinte);
}

interface Opcoes {
  method?: Metodo;
  body?: unknown;
}

function extrairMensagem(data: unknown): string | undefined {
  if (typeof data !== 'object' || data === null) return undefined;
  const corpo = data as Record<string, unknown>;
  if (typeof corpo.message === 'string' && corpo.message.trim()) return corpo.message;
  if (typeof corpo.mensagem === 'string' && corpo.mensagem.trim()) return corpo.mensagem;
  return undefined;
}

async function enviar(caminho: string, init: RequestInit): Promise<Response> {
  if (!API_BASE_URL) {
    throw new ApiError(MENSAGEM_SEM_CONFIGURACAO, 0);
  }
  try {
    return await fetch(`${API_BASE_URL}${caminho}`, { ...init, credentials: 'include' });
  } catch {
    throw new ApiError('Não foi possível conectar ao servidor. Verifique sua internet e tente novamente.', 0);
  }
}

async function lerJson(response: Response): Promise<unknown> {
  const texto = await response.text();
  if (!texto) return undefined;
  try {
    return JSON.parse(texto) as unknown;
  } catch {
    return undefined;
  }
}

async function obterTokenCsrf(): Promise<string> {
  const response = await enviar('/api/auth/csrf', { method: 'GET' });
  const data = await lerJson(response);
  if (!response.ok) {
    throw new ApiError(
      extrairMensagem(data) ?? `Não foi possível iniciar a sessão segura (HTTP ${response.status}).`,
      response.status
    );
  }
  if (typeof data !== 'object' || data === null || typeof (data as { token?: unknown }).token !== 'string') {
    throw new ApiError('O servidor não retornou um token de segurança válido.', response.status);
  }
  return (data as { token: string }).token;
}

/**
 * Faz uma requisição à API e devolve o JSON da resposta (ou undefined se vier vazia).
 * Requisições que alteram dados recebem automaticamente o token CSRF.
 */
export async function apiRequest(caminho: string, { method = 'GET', body }: Opcoes = {}): Promise<unknown> {
  const headers: Record<string, string> = { Accept: 'application/json' };
  if (method !== 'GET') {
    headers['X-XSRF-TOKEN'] = await obterTokenCsrf();
  }
  if (body !== undefined) {
    headers['Content-Type'] = 'application/json';
  }

  const response = await enviar(caminho, {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  const data = await lerJson(response);

  if (!response.ok) {
    // No login, 401 é só senha errada; nas demais rotas significa sessão expirada.
    if (response.status === 401 && caminho !== '/api/auth/login') {
      ouvintesDeSessaoExpirada.forEach((ouvinte) => ouvinte());
    }
    throw new ApiError(
      extrairMensagem(data) ?? `Não foi possível concluir a operação (HTTP ${response.status}).`,
      response.status
    );
  }
  return data;
}

export function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null;
}
