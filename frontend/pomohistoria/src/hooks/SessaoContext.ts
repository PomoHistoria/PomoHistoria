import { createContext } from 'react';
import type { User } from '../types';

export type SessaoContextValue = {
  /** Usuário logado, ou null. */
  usuario: User | null;
  /** true enquanto confere no servidor se há uma sessão ativa. */
  carregando: boolean;
  entrar: (email: string, senha: string) => Promise<User>;
  sair: () => Promise<void>;
  /** Troca os dados do usuário em memória (ex.: depois de salvar o perfil). */
  atualizarUsuario: (usuario: User) => void;
  /**
   * Esquece o usuário sem chamar o servidor (ex.: a conta acabou de ser excluída).
   * O aviso opcional aparece na tela de login.
   */
  limparSessao: (aviso?: string) => void;
  /** Aviso para a tela de login (ex.: "Sua conta foi excluída."). Some no próximo login. */
  aviso: string | null;
};

export const SessaoContext = createContext<SessaoContextValue | null>(null);
