import { useContext } from 'react'
import { PomodoroTimersContext } from './PomodoroTimersContext'

export function usePomodoroTimers() {
  const context = useContext(PomodoroTimersContext)
  if (context === null) {
    throw new Error('usePomodoroTimers deve ser usado dentro de PomodoroTimersProvider')
  }
  return context
}
