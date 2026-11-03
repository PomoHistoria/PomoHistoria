import { useEffect, useRef, useState } from 'react'
import type { ReactNode } from 'react'
import { usuarioApi } from '../services/usuarioApi'
import type { User } from '../types'
import {
  chaveDosCronometros,
  PomodoroTimersContext,
  type PomodoroTimerState,
  type TimerId,
} from './PomodoroTimersContext'
import { useSessao } from './useSessao'

const TIMER_IDS: TimerId[] = ['focus', 'rest']

/** Espera depois do último clique em +/- antes de salvar a duração na conta. */
const MS_ATE_SALVAR_NA_CONTA = 1000

type Duracoes = Record<TimerId, number>

function duracaoValida(valor: unknown): valor is number {
  return Number.isInteger(valor) && (valor as number) >= 1 && (valor as number) <= 99
}

/** Última duração usada, salva na conta (null se o servidor ainda não devolver esses campos). */
function duracoesDaConta(usuario: User | null): Duracoes | null {
  if (!usuario || !duracaoValida(usuario.duracaoFocoMinutos) || !duracaoValida(usuario.duracaoDescansoMinutos)) {
    return null
  }
  return { focus: usuario.duracaoFocoMinutos, rest: usuario.duracaoDescansoMinutos }
}

/** Parado no início: volta a mostrar a duração inteira, pronto para começar de novo. */
function parado(durationMinutes: number): PomodoroTimerState {
  return { durationMinutes, remainingSeconds: durationMinutes * 60, isRunning: false, endsAt: null }
}

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

/** Lê o estado salvo no navegador e recalcula o tempo pelo horário de término (o tempo passou com o site fechado). */
function lerDoNavegador(usuarioId: string): Record<TimerId, PomodoroTimerState> {
  try {
    const salvo = JSON.parse(localStorage.getItem(chaveDosCronometros(usuarioId)) ?? 'null') as
      Partial<Record<TimerId, PomodoroTimerState>> | null
    if (!salvo) return initialTimers

    const resultado = { ...initialTimers }
    for (const id of TIMER_IDS) {
      const timer = salvo[id]
      const valido =
        timer &&
        duracaoValida(timer.durationMinutes) &&
        typeof timer.remainingSeconds === 'number' && timer.remainingSeconds >= 0 &&
        typeof timer.isRunning === 'boolean' &&
        (timer.endsAt === null || typeof timer.endsAt === 'number')
      if (!valido) continue

      const restante = timer.isRunning && timer.endsAt !== null ? segundosRestantes(timer.endsAt) : timer.remainingSeconds
      if (timer.isRunning && restante > 0) {
        resultado[id] = { ...timer, remainingSeconds: restante }
      } else if (restante > 0) {
        resultado[id] = { ...timer, isRunning: false, endsAt: null }
      } else {
        // Terminou (inclusive com o site fechado): volta à duração usada.
        resultado[id] = parado(timer.durationMinutes)
      }
    }
    return resultado
  } catch {
    return initialTimers
  }
}

/**
 * Estado inicial: o do navegador, com a duração salva na conta nos cronômetros parados no início.
 * Um cronômetro contando ou pausado no meio continua como estava neste aparelho.
 */
function carregar(usuarioId: string | null, daConta: Duracoes | null): Record<TimerId, PomodoroTimerState> {
  if (!usuarioId) return initialTimers
  const resultado = lerDoNavegador(usuarioId)
  if (!daConta) return resultado
  for (const id of TIMER_IDS) {
    const timer = resultado[id]
    const noInicio = !timer.isRunning && timer.remainingSeconds === timer.durationMinutes * 60
    if (noInicio) resultado[id] = parado(daConta[id])
  }
  return resultado
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
 * A última duração usada também fica salva na conta, para valer em qualquer aparelho.
 */
export function PomodoroTimersProvider({ children }: { children: ReactNode }) {
  const { usuario } = useSessao()
  const usuarioId = usuario?.id ?? null
  // A key recria o estado quando o usuário muda (login/logout), carregando os cronômetros dele.
  return (
    <CronometrosDoUsuario
      key={usuarioId ?? 'visitante'}
      usuarioId={usuarioId}
      daConta={duracoesDaConta(usuario)}
    >
      {children}
    </CronometrosDoUsuario>
  )
}

function CronometrosDoUsuario({
  usuarioId,
  daConta,
  children,
}: {
  usuarioId: string | null
  /** Só é lido ao montar: depois, quem manda é o que o usuário ajusta aqui. */
  daConta: Duracoes | null
  children: ReactNode
}) {
  const { atualizarUsuario } = useSessao()
  const [timers, setTimers] = useState(() => carregar(usuarioId, daConta))
  // O que a conta já tem salvo: evita reenviar a mesma duração. Sem dado da conta, parte do estado atual.
  const salvoNaConta = useRef<Duracoes>(
    daConta ?? { focus: timers.focus.durationMinutes, rest: timers.rest.durationMinutes }
  )
  const duracaoFoco = timers.focus.durationMinutes
  const duracaoDescanso = timers.rest.durationMinutes

  useEffect(() => {
    if (usuarioId) salvar(usuarioId, timers)
  }, [usuarioId, timers])

  useEffect(() => {
    if (!usuarioId) return
    if (salvoNaConta.current.focus === duracaoFoco && salvoNaConta.current.rest === duracaoDescanso) return

    const id = window.setTimeout(() => {
      usuarioApi
        .atualizarCronometros(duracaoFoco, duracaoDescanso)
        .then((atualizado) => {
          salvoNaConta.current = { focus: duracaoFoco, rest: duracaoDescanso }
          atualizarUsuario(atualizado)
        })
        .catch((error: unknown) => {
          // Fica salvo só neste navegador; a próxima mudança tenta de novo.
          console.error('Não foi possível salvar a duração dos cronômetros na conta.', error)
        })
    }, MS_ATE_SALVAR_NA_CONTA)
    return () => window.clearTimeout(id)
  }, [usuarioId, duracaoFoco, duracaoDescanso, atualizarUsuario])

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
          // Ao chegar em zero, volta à duração usada, pronto para a próxima sessão.
          next[id] = remainingSeconds > 0
            ? { ...timer, remainingSeconds }
            : parado(timer.durationMinutes)
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
