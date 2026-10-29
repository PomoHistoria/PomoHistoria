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

/** O último módulo conhecido fica salvo no navegador, para o fundo certo aparecer já ao abrir a página. */
function chaveDoFundo(usuarioId: string) {
  return `pomohistoria:fundo:${usuarioId}`;
}

function lerSalvo(usuarioId: string): number | null {
  try {
    const valor = Number(localStorage.getItem(chaveDoFundo(usuarioId)));
    return Number.isInteger(valor) && valor >= 0 && valor <= 2 && localStorage.getItem(chaveDoFundo(usuarioId)) !== null
      ? valor
      : null;
  } catch {
    return null;
  }
}

function salvar(usuarioId: string, modulo: number) {
  try {
    localStorage.setItem(chaveDoFundo(usuarioId), String(modulo));
  } catch {
    // Armazenamento indisponível: o fundo só demora um instante a mais para aparecer.
  }
}

/** Apaga o fundo salvo do usuário (ex.: quando a conta é excluída). */
export function apagarFundoSalvo(usuarioId: string) {
  try {
    localStorage.removeItem(chaveDoFundo(usuarioId));
  } catch {
    // Nada salvo para apagar.
  }
}

function fundoPara(modulo: number): string {
  return FUNDO_POR_MODULO[modulo] ?? fundoInicial;
}

/** Chamado pelo questionário assim que o servidor registra a abertura de um módulo. */
export function registrarModuloIniciado(usuarioId: string, modulo: number) {
  const atual = lerSalvo(usuarioId);
  if (atual === null || modulo > atual) salvar(usuarioId, modulo);
}

/**
 * Imagem de fundo conforme o módulo mais avançado que o usuário já abriu:
 * nenhum = fundo inicial; módulo 1 = Grécia; módulo 2 = Era Vargas.
 *
 * Devolve null enquanto ainda não sabe qual é (primeiro acesso no aparelho): a página mostra a cor
 * sólida do site em vez de exibir um fundo errado e trocar em seguida.
 */
export function useFundoDoModulo(): string | null {
  const { usuario } = useSessao();
  const usuarioId = usuario?.id ?? null;
  const [modulo, setModulo] = useState<number | null>(() => (usuarioId ? lerSalvo(usuarioId) : null));

  useEffect(() => {
    if (!usuarioId) return;
    let ativo = true;
    obterProgresso()
      .then((progresso) => {
        const maisAvancado = Math.max(0, ...progresso.modulosIniciados);
        salvar(usuarioId, maisAvancado);
        if (ativo) setModulo(maisAvancado);
      })
      .catch(() => {
        // Sem resposta do servidor: usa o que já sabia ou, se não sabia nada, o fundo inicial.
        if (ativo) setModulo((atual) => atual ?? 0);
      });
    return () => {
      ativo = false;
    };
  }, [usuarioId]);

  return modulo === null ? null : fundoPara(modulo);
}
