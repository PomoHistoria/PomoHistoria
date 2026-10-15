import { usePomodoroTimers } from '../hooks/usePomodoroTimers'
import type { TimerId } from '../hooks/PomodoroTimersContext'
import styles from './Modal/PomodoroTimer.module.css'

type PomodoroTimerCardProps = {
  id: TimerId
  title: string
}

export default function PomodoroTimerCard({ id, title }: PomodoroTimerCardProps) {
  const { timers, adjustDuration, toggleTimer } = usePomodoroTimers()
  const timer = timers[id]
  const minutes = Math.floor(timer.remainingSeconds / 60)
  const seconds = timer.remainingSeconds % 60
  const buttonLabel = timer.remainingSeconds === 0
    ? 'REINICIAR'
    : timer.isRunning
      ? 'PAUSAR'
      : 'INICIAR'

  return (
    <article className={styles.timerCard} aria-label={title}>
      <h2 className={styles.timerTitle}>{title}</h2>
      <div className={styles.timerControls}>
        <button
          className={styles.adjustButton}
          type="button"
          aria-label={`Diminuir duração de ${title}`}
          disabled={timer.isRunning || timer.durationMinutes === 1}
          onClick={() => adjustDuration(id, -1)}
        >
          -
        </button>
        <output className={styles.time} aria-live="off">
          {String(minutes).padStart(2, '0')}:{String(seconds).padStart(2, '0')}
        </output>
        <button
          className={styles.adjustButton}
          type="button"
          aria-label={`Aumentar duração de ${title}`}
          disabled={timer.isRunning || timer.durationMinutes === 99}
          onClick={() => adjustDuration(id, 1)}
        >
          +
        </button>
      </div>
      <button
        className={styles.startButton}
        type="button"
        aria-pressed={timer.isRunning}
        onClick={() => toggleTimer(id)}
      >
        {buttonLabel}
      </button>
    </article>
  )
}
