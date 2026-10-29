import type { ReactNode } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useSessao } from '../hooks/useSessao';
import type { User } from '../types';

function Carregando() {
  return (
    <p role="status" style={{ padding: '2rem', textAlign: 'center' }}>
      Carregando...
    </p>
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
