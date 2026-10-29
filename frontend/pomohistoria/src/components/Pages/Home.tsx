import { Link } from 'react-router-dom'
import { useFundoDoModulo } from '../../hooks/useFundoDoModulo'
import { useSessao } from '../../hooks/useSessao'
import logo from '../../assets/POMOHISTÓRIA.webp'
import PomodoroTimerCard from '../PomodoroTimerCard'
import { usePomodoroTimers } from '../../hooks/usePomodoroTimers'
import { timerAtivo, type TimerId } from '../../hooks/PomodoroTimersContext'
import timerStyles from '../Modal/PomodoroTimer.module.css'
import styles from '../Modal/Home.module.css'

const CRONOMETROS: { id: TimerId; titulo: string }[] = [
  { id: 'focus', titulo: 'Sessão de Foco' },
  { id: 'rest', titulo: 'Sessão de Descanso' },
]

export default function Home() {
  const background = useFundoDoModulo()
  const { usuario } = useSessao()
  const { timers } = usePomodoroTimers()
  const ativos = CRONOMETROS.filter(({ id }) => timerAtivo(timers[id]))
  return (
    <main className={styles.home}>
      <img
        className={styles.background}
        src={background}
        alt="Praça ensolarada com um estudante no centro"
      />
      <div className={styles.shade} aria-hidden="true" />

      <header className={styles.header}>
        <img className={styles.logo} src={logo} alt="PomoHistória" />

        <details className={styles.profileMenu}>
          <summary className={styles.profileButton}>
            <span className={styles.profileIcon} aria-hidden="true">●</span>
            <span>Perfil</span>
          </summary>
          <div className={styles.profileDropdown}>
            <Link className={styles.profileLink} to="/profile">Meu perfil</Link>
            {usuario?.papel === 'ADMIN' && (
              <Link className={styles.profileLink} to="/admin">Administração</Link>
            )}
          </div>
        </details>
      </header>

      {/* Só os cronômetros ativados na página Pomodoro aparecem no início. */}
      {ativos.length > 0 && (
        <section className={styles.timerArea} aria-label="Temporizadores Pomodoro ativos">
          <div className={timerStyles.timers}>
            {ativos.map(({ id, titulo }) => (
              <PomodoroTimerCard key={id} id={id} title={titulo} />
            ))}
          </div>
        </section>
      )}

      <nav className={styles.actions} aria-label="Atividades">
        <Link className={styles.action} to="/temporizador">Pomodoro</Link>
        <Link className={styles.action} to="/questionario">Questionário</Link>
      </nav>
    </main>
  )
}