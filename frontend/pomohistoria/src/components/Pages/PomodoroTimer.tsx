import { Link } from 'react-router-dom'
import background from '../../assets/FundoHomeInicial.jpeg'
import logo from '../../assets/POMOHISTÓRIA.png'
import PomodoroTimerCard from '../PomodoroTimerCard'
import styles from '../Modal/PomodoroTimer.module.css'

export default function PomodoroTimer() {
  return (
    <main className={styles.page}>
      <img
        className={styles.background}
        src={background}
        alt=""
        aria-hidden="true"
      />
      <div className={styles.shade} aria-hidden="true" />

      <header className={styles.header}>
        <img className={styles.logo} src={logo} alt="PomoHistória" />

        <details className={styles.profileMenu}>
          <summary className={styles.profileButton}>
            <span className={styles.profileIcon} aria-hidden="true">●</span>
            <span>Perfil</span>
          </summary>
          <nav className={styles.menuDropdown} aria-label="Navegação">
            <Link className={styles.link} to="/profile">Meu perfil</Link>
            <Link className={styles.link} to="/">Início</Link>
          </nav>
        </details>
      </header>

      <section className={styles.timers} aria-label="Configuração dos temporizadores">
        <PomodoroTimerCard id="focus" title="Sessão de Foco" />
        <PomodoroTimerCard id="rest" title="Sessão de Descanso" />
      </section>
    </main>
  )
}
