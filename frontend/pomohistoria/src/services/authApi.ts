import type {
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RegisterResponse,
  User,
} from '../types';
import { apiRequest, ApiError, isRecord } from './http';

export function isUser(value: unknown): value is User {
  return (
    isRecord(value) &&
    typeof value.id === 'string' &&
    typeof value.nome === 'string' &&
    typeof value.email === 'string'
  );
}

function exigirRespostaDeAuth<T extends LoginResponse | RegisterResponse>(data: unknown): T {
  if (!isRecord(data) || !isUser(data.usuario)) {
    throw new ApiError('O servidor retornou uma resposta inválida.', 200);
  }
  return data as unknown as T;
}

export const authApi = {
  async registrar(dados: RegisterRequest): Promise<RegisterResponse> {
    return exigirRespostaDeAuth(await apiRequest('/api/auth/register', { method: 'POST', body: dados }));
  },

  async entrar(dados: LoginRequest): Promise<LoginResponse> {
    return exigirRespostaDeAuth(await apiRequest('/api/auth/login', { method: 'POST', body: dados }));
  },

  /** Usuário da sessão atual, ou null se não houver login. */
  async usuarioAtual(): Promise<User | null> {
    try {
      const data = await apiRequest('/api/auth/me');
      if (!isUser(data)) throw new ApiError('O servidor retornou uma resposta inválida.', 200);
      return data;
    } catch (error) {
      if (error instanceof ApiError && error.naoAutenticado) return null;
      throw error;
    }
  },

  async sair(): Promise<void> {
    await apiRequest('/api/auth/logout', { method: 'POST' });
  },
};
