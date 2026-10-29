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
