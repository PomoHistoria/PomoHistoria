import { useState } from 'react'
import type { ChangeEvent, SubmitEvent } from 'react'
import { Link } from 'react-router-dom'
import logo from '../../assets/POMOHISTÓRIA.png'
import styles from '../Modal/Profile.module.css'

export default function Profile() {
  const [photo, setPhoto] = useState<string | null>(null)
  const [saved, setSaved] = useState(false)

  function handlePhotoChange(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0]
    if (!file) return

    const reader = new FileReader()
    reader.addEventListener('load', () => {
      if (typeof reader.result === 'string') setPhoto(reader.result)
    })
    reader.readAsDataURL(file)
  }

  function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault();
    setSaved(true);
  }

  return (
    <main className={styles.profile}>
      <aside className={styles.sidebar}>
        <img className={styles.sidebarLogo} src={logo} alt="PomoHistória" />
        <div className={styles.userSummary}>
          <div className={styles.smallAvatar} aria-hidden="true">
            <span />
          </div>
          <strong>USUÁRIO</strong>
        </div>
        <Link className={styles.logout} to="/login">SAIR</Link>
      </aside>

      <header className={styles.header}>
        <img className={styles.logo} src={logo} alt="PomoHistória" />
      </header>

      <section className={styles.content} aria-labelledby="profile-title">
        <Link className={styles.back} to="/">VOLTAR</Link>

        <form className={styles.form} onSubmit={handleSubmit}>
          <div className={styles.photoColumn}>
            <div className={styles.avatar}>
              {photo ? <img src={photo} alt="Foto de perfil" /> : <span aria-hidden="true" />}
            </div>
            <label className={styles.photoLabel} htmlFor="profile-photo">EDITAR FOTO</label>
            <input
              className={styles.photoInput}
              id="profile-photo"
              type="file"
              accept="image/*"
              onChange={handlePhotoChange}
            />
          </div>

          <div className={styles.fields}>
            <h1 id="profile-title">NOME COMPLETO</h1>
            <input aria-label="Nome completo" placeholder="Digite seu nome completo" />

            <h2>TELEFONE</h2>
            <input aria-label="Telefone" type="tel" placeholder="Digite seu telefone" />

            <div className={styles.fieldGrid}>
              <label>
                <span>EMAIL</span>
                <input aria-label="Email" type="email" placeholder="Digite seu email" />
              </label>
              <label>
                <span>CONFIRMAR EMAIL</span>
                <input aria-label="Confirmar email" type="email" placeholder="Confirme seu email" />
              </label>
              <label>
                <span>NOVA SENHA</span>
                <input aria-label="Nova senha" type="password" placeholder="Digite sua nova senha" />
              </label>
              <label>
                <span>CONFIRMAR SENHA</span>
                <input aria-label="Confirmar senha" type="password" placeholder="Confirme sua nova senha" />
              </label>
            </div>
          </div>

          <button className={styles.save} type="submit">SALVAR</button>
          {saved && <p className={styles.feedback} role="status">Alterações salvas.</p>}
        </form>
      </section>
    </main>
  );
}