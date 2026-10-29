import type { PerfilRequest, User } from '../types';
import { isUser } from './authApi';
import { apiRequest, ApiError } from './http';

function exigirUsuario(data: unknown): User {
  if (!isUser(data)) throw new ApiError('O servidor retornou uma resposta inválida.', 200);
  return data;
}

export const usuarioApi = {
  async atualizarPerfil(dados: PerfilRequest): Promise<User> {
    return exigirUsuario(await apiRequest('/api/usuarios/me', { method: 'PUT', body: dados }));
  },

  /** foto null remove a foto atual. */
  async atualizarFoto(foto: string | null): Promise<User> {
    return exigirUsuario(await apiRequest('/api/usuarios/me/foto', { method: 'PUT', body: { foto } }));
  },

  /** Exclui a própria conta (só usuários comuns). O servidor encerra a sessão. */
  async excluirConta(senhaAtual: string): Promise<void> {
    await apiRequest('/api/usuarios/me', { method: 'DELETE', body: { senhaAtual } });
  },
};

/** Reduz a imagem para no máximo 256x256 (JPEG), para a foto ficar leve (~30 KB). */
export function reduzirImagem(arquivo: File, tamanho = 256): Promise<string> {
  return new Promise((resolve, reject) => {
    if (!arquivo.type.startsWith('image/')) {
      reject(new Error('Escolha um arquivo de imagem.'));
      return;
    }
    const url = URL.createObjectURL(arquivo);
    const imagem = new Image();
    imagem.onload = () => {
      URL.revokeObjectURL(url);
      // Recorta o centro em quadrado e reduz.
      const lado = Math.min(imagem.width, imagem.height);
      const canvas = document.createElement('canvas');
      canvas.width = tamanho;
      canvas.height = tamanho;
      const contexto = canvas.getContext('2d');
      if (!contexto) {
        reject(new Error('Seu navegador não conseguiu processar a imagem.'));
        return;
      }
      contexto.drawImage(
        imagem,
        (imagem.width - lado) / 2,
        (imagem.height - lado) / 2,
        lado,
        lado,
        0,
        0,
        tamanho,
        tamanho
      );
      resolve(canvas.toDataURL('image/jpeg', 0.85));
    };
    imagem.onerror = () => {
      URL.revokeObjectURL(url);
      reject(new Error('Não foi possível ler a imagem escolhida.'));
    };
    imagem.src = url;
  });
}
