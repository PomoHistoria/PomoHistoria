import { useEffect, useState } from 'react'
import type { ReactNode } from 'react'
import {
  PomodoroTimersContext,
  type PomodoroTimerState,
  type TimerId,
} from './PomodoroTimersContext'

const initialTimers: Record<TimerId, PomodoroTimerState> = {
  focus: {
    durationMinutes: 25,
    remainingSeconds: 25 * 60,
    isRunning: false,
    endsAt: null,
  },
  rest: {
    durationMinutes: 5,
    remainingSeconds: 5 * 60,
    isRunning: false,
    endsAt: null,
  },
}

export function PomodoroTimersProvider({ children }: { children: ReactNode }) {
  const [timers, setTimers] = useState(initialTimers)

  useEffect(() => {
    const interval = window.setInterval(() => {
      setTimers((current) => {
        let changed = false
        const next = { ...current }

        for (const id of ['focus', 'rest'] as const) {
          const timer = current[id]
          if (!timer.isRunning || timer.endsAt === null) continue

          const remainingSeconds = Math.max(0, Math.ceil((timer.endsAt - Date.now()) / 1000))
          if (remainingSeconds === timer.remainingSeconds) continue

          changed = true
          next[id] = {
            ...timer,
            remainingSeconds,
            isRunning: remainingSeconds > 0,
            endsAt: remainingSeconds > 0 ? timer.endsAt : null
          }
        }

        return changed ? next : current
      })
    }, 250)

    return () => window.clearInterval(interval)
  }, [])

  function adjustDuration(id: TimerId, amount: number) {
    setTimers((current) => {
      const timer = current[id]
      if (timer.isRunning) return current

      const durationMinutes = Math.min(99, Math.max(1, timer.durationMinutes + amount))
      return {
        ...current,
        [id]: {
          ...timer,
          durationMinutes,
          remainingSeconds: durationMinutes * 60,
        },
      }
    })
  }

  function toggleTimer(id: TimerId) {
    setTimers((current) => {
      const timer = current[id]

      if (timer.isRunning) {
        const remainingSeconds = timer.endsAt === null
          ? timer.remainingSeconds
          : Math.max(0, Math.ceil((timer.endsAt - Date.now()) / 1000))
        return {
          ...current,
          [id]: {
            ...timer,
            remainingSeconds,
            isRunning: false,
            endsAt: null
          },
        }
      }

      const remainingSeconds = timer.remainingSeconds === 0
        ? timer.durationMinutes * 60
        : timer.remainingSeconds
      return {
        ...current,
        [id]: {
          ...timer,
          remainingSeconds,
          isRunning: true,
          endsAt: Date.now() + remainingSeconds * 1000,
        },
      }
    })
  }

  return (
    <PomodoroTimersContext.Provider value={{ timers, adjustDuration, toggleTimer }}>
      {children}
    </PomodoroTimersContext.Provider>
  )
}
