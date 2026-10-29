import { useCallback, useEffect, useMemo, useState } from 'react';
import type { ReactNode } from 'react';
import { authApi } from '../services/authApi';
import { aoExpirarSessao } from '../services/http';
import type { User } from '../types';
import { SessaoContext } from './SessaoContext';

export function SessaoProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<User | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [aviso, setAviso] = useState<string | null>(null);

  useEffect(() => {
    let ativo = true;
    authApi
      .usuarioAtual()
      .then((atual) => {
        if (ativo) setUsuario(atual);
      })
      .catch((error: unknown) => {
        // Sem conexão com o servidor: segue como deslogado; a próxima ação mostra o erro.
        console.error('Não foi possível verificar a sessão.', error);
      })
      .finally(() => {
        if (ativo) setCarregando(false);
      });
    return () => {
      ativo = false;
    };
  }, []);

  // Sessão expirada no servidor: limpa o usuário e o RequireAuth manda para o login.
  useEffect(() => aoExpirarSessao(() => setUsuario(null)), []);

  const entrar = useCallback(async (email: string, senha: string) => {
    const resposta = await authApi.entrar({ email, senha });
    setAviso(null);
    setUsuario(resposta.usuario);
    return resposta.usuario;
  }, []);

  const sair = useCallback(async () => {
    try {
      await authApi.sair();
    } finally {
      // Mesmo se o servidor falhar, a tela deixa de mostrar o usuário.
      setUsuario(null);
    }
  }, []);

  const atualizarUsuario = useCallback((atualizado: User) => setUsuario(atualizado), []);
  const limparSessao = useCallback((novoAviso?: string) => {
    setAviso(novoAviso ?? null);
    setUsuario(null);
  }, []);

  const value = useMemo(
    () => ({ usuario, carregando, entrar, sair, atualizarUsuario, limparSessao, aviso }),
    [usuario, carregando, entrar, sair, atualizarUsuario, limparSessao, aviso]
  );

  return <SessaoContext.Provider value={value}>{children}</SessaoContext.Provider>;
}
