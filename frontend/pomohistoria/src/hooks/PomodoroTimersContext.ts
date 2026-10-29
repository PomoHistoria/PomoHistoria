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

/**
 * Cronômetro "ativo": rodando, ou pausado no meio da sessão (para poder ser retomado).
 * Parado no tempo cheio ou já terminado (00:00) não conta.
 */
export function timerAtivo(timer: PomodoroTimerState): boolean {
  if (timer.isRunning) return true
  return timer.remainingSeconds > 0 && timer.remainingSeconds < timer.durationMinutes * 60
}
