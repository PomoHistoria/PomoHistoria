import type {
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RegisterResponse,
} from '../types';

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080')
  .replace(/\/+$/, '');

function getErrorMessage(data: unknown): string | undefined {
  if (typeof data !== 'object' || data === null) return undefined;

  const response = data as Record<string, unknown>;
  if (typeof response.message === 'string') return response.message;
  if (typeof response.mensagem === 'string') return response.mensagem;
  return undefined;
}

async function post<TResponse>(path: string, body: object): Promise<TResponse> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(body),
  });

  const responseText = await response.text();
  let data: unknown;

  if (responseText) {
    try {
      data = JSON.parse(responseText) as unknown;
    } catch {
      if (response.ok) {
        throw new Error('O servidor retornou uma resposta inválida.');
      }
    }
  }

  if (!response.ok) {
    throw new Error(
      getErrorMessage(data) ?? `Não foi possível concluir a operação (HTTP ${response.status}).`
    );
  }

  if (typeof data !== 'object' || data === null) {
    throw new Error('O servidor retornou uma resposta inválida.');
  }

  return data as TResponse;
}

export const authApi = {
  registrar(dados: RegisterRequest): Promise<RegisterResponse> {
    return post<RegisterResponse>('/api/auth/register', dados);
  },

  entrar(dados: LoginRequest): Promise<LoginResponse> {
    return post<LoginResponse>('/api/auth/login', dados);
  },
};
