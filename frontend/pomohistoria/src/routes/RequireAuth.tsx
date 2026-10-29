import { useEffect, useState } from 'react';
import type { ReactNode } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useSessao } from '../hooks/useSessao';
import type { User } from '../types';

/** Depois desse tempo sem resposta, avisa que o servidor pode estar acordando. */
const MS_ATE_AVISAR_DEMORA = 3000;

function Carregando() {
  const [demorando, setDemorando] = useState(false);

  useEffect(() => {
    const id = window.setTimeout(() => setDemorando(true), MS_ATE_AVISAR_DEMORA);
    return () => window.clearTimeout(id);
  }, []);

  return (
    <div
      role="status"
      aria-live="polite"
      style={{
        display: 'grid',
        placeContent: 'center',
        gap: '0.5rem',
        minHeight: '100dvh',
        padding: '2rem',
        boxSizing: 'border-box',
        background: '#f8f1e8',
        color: '#4d1f00',
        fontFamily: 'Arial, Helvetica, sans-serif',
        textAlign: 'center',
      }}
    >
      {demorando ? (
        <>
          <strong style={{ fontSize: '1.1rem' }}>O servidor está iniciando…</strong>
          <span style={{ color: '#705c49', fontSize: '0.95rem', maxWidth: '28rem' }}>
            Depois de um tempo sem uso, o primeiro acesso pode levar até 1 minuto. Aguarde, a página abre sozinha.
          </span>
        </>
      ) : (
        <span>Carregando...</span>
      )}
    </div>
  );
}

/**
 * Mostra a página só para quem está logado; os demais vão para o login e voltam depois.
 * Com {@code papel}, exige também esse papel (quem não tem vai para o início).
 */
export default function RequireAuth({ children, papel }: { children: ReactNode; papel?: User['papel'] }) {
  const { usuario, carregando } = useSessao();
  const location = useLocation();

  if (carregando) return <Carregando />;
  if (!usuario) return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  if (papel && usuario.papel !== papel) return <Navigate to="/" replace />;

  return <>{children}</>;
}

/**
 * Páginas de login e cadastro: quem já está logado vai para a página que tentou abrir
 * (o mesmo destino que o login usa) ou para o início.
 */
export function SomenteVisitante({ children }: { children: ReactNode }) {
  const { usuario, carregando } = useSessao();
  const location = useLocation();
  const destino = (location.state as { from?: string } | null)?.from ?? '/';

  if (carregando) return <Carregando />;
  if (usuario) return <Navigate to={destino} replace />;
  return <>{children}</>;
}
