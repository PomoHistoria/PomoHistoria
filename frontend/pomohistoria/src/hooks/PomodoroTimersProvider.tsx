import { useEffect, useState } from 'react'
import type { ReactNode } from 'react'
import {
  chaveDosCronometros,
  PomodoroTimersContext,
  type PomodoroTimerState,
  type TimerId,
} from './PomodoroTimersContext'
import { useSessao } from './useSessao'

const TIMER_IDS: TimerId[] = ['focus', 'rest']

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

function segundosRestantes(endsAt: number) {
  return Math.max(0, Math.ceil((endsAt - Date.now()) / 1000))
}

/** Lê o estado salvo e recalcula o tempo pelo horário de término (o tempo passou com o site fechado). */
function carregar(usuarioId: string | null): Record<TimerId, PomodoroTimerState> {
  if (!usuarioId) return initialTimers
  try {
    const salvo = JSON.parse(localStorage.getItem(chaveDosCronometros(usuarioId)) ?? 'null') as
      Partial<Record<TimerId, PomodoroTimerState>> | null
    if (!salvo) return initialTimers

    const resultado = { ...initialTimers }
    for (const id of TIMER_IDS) {
      const timer = salvo[id]
      const valido =
        timer &&
        Number.isInteger(timer.durationMinutes) && timer.durationMinutes >= 1 && timer.durationMinutes <= 99 &&
        typeof timer.remainingSeconds === 'number' && timer.remainingSeconds >= 0 &&
        typeof timer.isRunning === 'boolean' &&
        (timer.endsAt === null || typeof timer.endsAt === 'number')
      if (!valido) continue

      if (timer.isRunning && timer.endsAt !== null) {
        const restante = segundosRestantes(timer.endsAt)
        resultado[id] = restante > 0
          ? { ...timer, remainingSeconds: restante }
          : { ...timer, remainingSeconds: 0, isRunning: false, endsAt: null }
      } else {
        resultado[id] = { ...timer, isRunning: false, endsAt: null }
      }
    }
    return resultado
  } catch {
    return initialTimers
  }
}

function salvar(usuarioId: string, timers: Record<TimerId, PomodoroTimerState>) {
  try {
    localStorage.setItem(chaveDosCronometros(usuarioId), JSON.stringify(timers))
  } catch {
    // Armazenamento indisponível (ex.: modo privado): os cronômetros só não sobrevivem ao recarregar.
  }
}

/**
 * Cronômetros Pomodoro compartilhados por todas as páginas. Continuam contando ao trocar de página,
 * ao recarregar e com o site fechado, porque guardam o horário de término.
 */
export function PomodoroTimersProvider({ children }: { children: ReactNode }) {
  const usuarioId = useSessao().usuario?.id ?? null
  // A key recria o estado quando o usuário muda (login/logout), carregando os cronômetros dele.
  return (
    <CronometrosDoUsuario key={usuarioId ?? 'visitante'} usuarioId={usuarioId}>
      {children}
    </CronometrosDoUsuario>
  )
}

function CronometrosDoUsuario({ usuarioId, children }: { usuarioId: string | null; children: ReactNode }) {
  const [timers, setTimers] = useState(() => carregar(usuarioId))

  useEffect(() => {
    if (usuarioId) salvar(usuarioId, timers)
  }, [usuarioId, timers])

  useEffect(() => {
    const interval = window.setInterval(() => {
      setTimers((current) => {
        let changed = false
        const next = { ...current }

        for (const id of TIMER_IDS) {
          const timer = current[id]
          if (!timer.isRunning || timer.endsAt === null) continue

          const remainingSeconds = segundosRestantes(timer.endsAt)
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
          : segundosRestantes(timer.endsAt)
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
