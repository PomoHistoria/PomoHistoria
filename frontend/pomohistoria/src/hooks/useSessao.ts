import { useContext } from 'react';
import { SessaoContext } from './SessaoContext';

export function useSessao() {
  const context = useContext(SessaoContext);
  if (context === null) {
    throw new Error('useSessao deve ser usado dentro de SessaoProvider');
  }
  return context;
}
