import { Link } from 'react-router-dom'
import { useFundoDoModulo } from '../../hooks/useFundoDoModulo'
import { useSessao } from '../../hooks/useSessao'
import logo from '../../assets/POMOHISTÓRIA.webp'
import PomodoroTimerCard from '../PomodoroTimerCard'
import timerStyles from '../Modal/PomodoroTimer.module.css'
import styles from '../Modal/Home.module.css'

export default function Home() {
  const background = useFundoDoModulo()
  const { usuario } = useSessao()
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

      <section className={styles.timerArea} aria-label="Temporizadores Pomodoro">
        <div className={timerStyles.timers}>
          <PomodoroTimerCard id="focus" title="Sessão de Foco" />
          <PomodoroTimerCard id="rest" title="Sessão de Descanso" />
        </div>
      </section>

      <nav className={styles.actions} aria-label="Atividades">
        <Link className={styles.action} to="/temporizador">Pomodoro</Link>
        <Link className={styles.action} to="/questionario">Questionário</Link>
      </nav>
    </main>
  )
}