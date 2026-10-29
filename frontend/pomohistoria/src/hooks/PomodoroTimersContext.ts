import { createContext } from 'react'

export type TimerId = 'focus' | 'rest'

export type PomodoroTimerState = {
  durationMinutes: number
  remainingSeconds: number
  isRunning: boolean
  endsAt: number | null
}

export type PomodoroTimersContextValue = {
  timers: Record<TimerId, PomodoroTimerState>
  adjustDuration: (id: TimerId, amount: number) => void
  toggleTimer: (id: TimerId) => void
}

export const PomodoroTimersContext = createContext<PomodoroTimersContextValue | null>(null)

/** Chave onde os cronômetros de cada usuário ficam salvos no navegador. */
export function chaveDosCronometros(usuarioId: string) {
  return `pomohistoria:cronometros:${usuarioId}`
}

/** Apaga os cronômetros salvos do usuário (ex.: quando a conta é excluída). */
export function apagarCronometrosSalvos(usuarioId: string) {
  try {
    localStorage.removeItem(chaveDosCronometros(usuarioId))
  } catch {
    // Armazenamento indisponível: não há nada salvo para apagar.
  }
}

/**
 * Cronômetro "ativo" = contando agora. É o único que aparece no início;
 * pausado, parado ou terminado ele só aparece na página Pomodoro.
 */
export function timerAtivo(timer: PomodoroTimerState): boolean {
  return timer.isRunning
}
