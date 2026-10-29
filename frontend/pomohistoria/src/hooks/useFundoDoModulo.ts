import { useEffect, useState } from 'react';
import fundoInicial from '../assets/FundoHomeInicial.webp';
import fundoGrecia from '../assets/fundo_grecia.webp';
import fundoEraVargas from '../assets/fundo_era_vargas.webp';
import { obterProgresso } from '../services/questionarioApi';
import { useSessao } from './useSessao';

/** Fundo de cada módulo: 1 = Antiguidade Clássica, 2 = Era Vargas. */
const FUNDO_POR_MODULO: Record<number, string> = {
  1: fundoGrecia,
  2: fundoEraVargas,
};

// Guarda o último módulo iniciado para a troca de página não piscar o fundo inicial.
let ultimoConhecido: { usuarioId: string; modulo: number } | null = null;

function fundoPara(modulo: number): string {
  return FUNDO_POR_MODULO[modulo] ?? fundoInicial;
}

/** Chamado pelo questionário assim que o servidor registra a abertura de um módulo. */
export function registrarModuloIniciado(usuarioId: string, modulo: number) {
  if (!ultimoConhecido || ultimoConhecido.usuarioId !== usuarioId || modulo > ultimoConhecido.modulo) {
    ultimoConhecido = { usuarioId, modulo };
  }
}

/**
 * Imagem de fundo conforme o módulo mais avançado que o usuário já abriu:
 * nenhum = fundo inicial; módulo 1 = Grécia; módulo 2 = Era Vargas.
 */
export function useFundoDoModulo(): string {
  const { usuario } = useSessao();
  const usuarioId = usuario?.id ?? null;
  const [modulo, setModulo] = useState(() =>
    ultimoConhecido && ultimoConhecido.usuarioId === usuarioId ? ultimoConhecido.modulo : 0
  );

  useEffect(() => {
    if (!usuarioId) return;
    let ativo = true;
    obterProgresso()
      .then((progresso) => {
        const maisAvancado = Math.max(0, ...progresso.modulosIniciados);
        ultimoConhecido = { usuarioId, modulo: maisAvancado };
        if (ativo) setModulo(maisAvancado);
      })
      .catch(() => {
        // Sem progresso disponível: mantém o fundo atual.
      });
    return () => {
      ativo = false;
    };
  }, [usuarioId]);

  return fundoPara(modulo);
}
