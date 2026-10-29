import { useState } from 'react'
import type { ChangeEvent, SubmitEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useSessao } from '../../hooks/useSessao'
import { reduzirImagem, usuarioApi } from '../../services/usuarioApi'
import { validateEmail, validateName, validatePassword } from '../../utils/validation'
import logo from '../../assets/POMOHISTÓRIA.webp'
import styles from '../Modal/Profile.module.css'

const TELEFONE_VALIDO = /^[0-9()+\-\s]{8,20}$/

type Feedback = { tipo: 'sucesso' | 'erro'; texto: string } | null

export default function Profile() {
  const { usuario, sair, atualizarUsuario } = useSessao()
  const navigate = useNavigate()

  // A rota é protegida por RequireAuth, então o usuário já está carregado aqui.
  const [nome, setNome] = useState(usuario?.nome ?? '')
  const [telefone, setTelefone] = useState(usuario?.telefone ?? '')
  const [email, setEmail] = useState(usuario?.email ?? '')
  const [confirmaEmail, setConfirmaEmail] = useState('')
  const [novaSenha, setNovaSenha] = useState('')
  const [confirmaSenha, setConfirmaSenha] = useState('')
  const [senhaAtual, setSenhaAtual] = useState('')
  const [foto, setFoto] = useState<string | null>(usuario?.foto ?? null)
  const [fotoAlterada, setFotoAlterada] = useState(false)
  const [salvando, setSalvando] = useState(false)
  const [feedback, setFeedback] = useState<Feedback>(null)

  async function handleSair() {
    await sair().catch(() => undefined)
    navigate('/login', { replace: true })
  }

  async function handlePhotoChange(event: ChangeEvent<HTMLInputElement>) {
    const arquivo = event.target.files?.[0]
    event.target.value = ''
    if (!arquivo) return
    try {
      setFoto(await reduzirImagem(arquivo))
      setFotoAlterada(true)
      setFeedback(null)
    } catch (error) {
      setFeedback({ tipo: 'erro', texto: error instanceof Error ? error.message : 'Imagem inválida.' })
    }
  }

  function validar(): string | null {
    const emailNormalizado = email.trim().toLowerCase()
    const trocaEmail = emailNormalizado !== usuario?.email
    const trocaSenha = novaSenha.length > 0

    if (!validateName(nome)) return 'O nome deve ter no mínimo 3 caracteres.'
    if (telefone.trim() && !TELEFONE_VALIDO.test(telefone.trim())) return 'Telefone inválido.'
    if (!validateEmail(emailNormalizado)) return 'Email inválido.'
    if (trocaEmail && confirmaEmail.trim().toLowerCase() !== emailNormalizado) {
      return 'Confirme o novo email: os dois campos devem ser iguais.'
    }
    if (trocaSenha && !validatePassword(novaSenha, 8)) return 'A nova senha deve ter no mínimo 8 caracteres.'
    if (trocaSenha && novaSenha !== confirmaSenha) return 'As senhas não coincidem.'
    if ((trocaEmail || trocaSenha) && !senhaAtual) return 'Informe a senha atual para trocar email ou senha.'
    return null
  }

  async function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault()
    const erro = validar()
    if (erro) {
      setFeedback({ tipo: 'erro', texto: erro })
      return
    }

    setSalvando(true)
    setFeedback(null)
    try {
      if (fotoAlterada) {
        atualizarUsuario(await usuarioApi.atualizarFoto(foto))
        setFotoAlterada(false)
      }
      const atualizado = await usuarioApi.atualizarPerfil({
        nome: nome.trim(),
        telefone: telefone.trim(),
        email: email.trim().toLowerCase(),
        senhaAtual: senhaAtual || null,
        novaSenha: novaSenha || null,
      })
      atualizarUsuario(atualizado)
      setEmail(atualizado.email)
      setConfirmaEmail('')
      setNovaSenha('')
      setConfirmaSenha('')
      setSenhaAtual('')
      setFeedback({ tipo: 'sucesso', texto: 'Alterações salvas.' })
    } catch (error) {
      setFeedback({
        tipo: 'erro',
        texto: error instanceof Error ? error.message : 'Não foi possível salvar as alterações.',
      })
    } finally {
      setSalvando(false)
    }
  }

  return (
    <main className={styles.profile}>
      <aside className={styles.sidebar}>
        <img className={styles.sidebarLogo} src={logo} alt="PomoHistória" />
        <div className={styles.userSummary}>
          <div className={styles.smallAvatar} aria-hidden="true">
            {usuario?.foto ? <img src={usuario.foto} alt="" /> : <span />}
          </div>
          <strong>{usuario?.nome ?? 'USUÁRIO'}</strong>
        </div>
        <Link
          className={styles.logout}
          to="/login"
          onClick={(event) => {
            event.preventDefault()
            void handleSair()
          }}
        >
          SAIR
        </Link>
      </aside>

      <header className={styles.header}>
        <img className={styles.logo} src={logo} alt="PomoHistória" />
      </header>

      <section className={styles.content} aria-labelledby="profile-title">
        <Link className={styles.back} to="/">VOLTAR</Link>

        <form className={styles.form} onSubmit={handleSubmit} noValidate>
          <div className={styles.photoColumn}>
            <div className={styles.avatar}>
              {foto ? <img src={foto} alt="Foto de perfil" /> : <span aria-hidden="true" />}
            </div>
            <label className={styles.photoLabel} htmlFor="profile-photo">EDITAR FOTO</label>
            <input
              className={styles.photoInput}
              id="profile-photo"
              type="file"
              accept="image/jpeg,image/png,image/webp"
              onChange={(event) => void handlePhotoChange(event)}
              disabled={salvando}
            />
          </div>

          <div className={styles.fields}>
            <h1 id="profile-title">NOME COMPLETO</h1>
            <input
              aria-label="Nome completo"
              placeholder="Digite seu nome completo"
              value={nome}
              onChange={(event) => setNome(event.target.value)}
              maxLength={100}
              autoComplete="name"
            />

            <h2>TELEFONE</h2>
            <input
              aria-label="Telefone"
              type="tel"
              placeholder="Digite seu telefone"
              value={telefone}
              onChange={(event) => setTelefone(event.target.value)}
              maxLength={20}
              autoComplete="tel"
            />

            <div className={styles.fieldGrid}>
              <label>
                <span>EMAIL</span>
                <input
                  aria-label="Email"
                  type="email"
                  placeholder="Digite seu email"
                  value={email}
                  onChange={(event) => setEmail(event.target.value)}
                  maxLength={255}
                  autoComplete="email"
                />
              </label>
              <label>
                <span>CONFIRMAR EMAIL</span>
                <input
                  aria-label="Confirmar email"
                  type="email"
                  placeholder="Só ao trocar o email"
                  value={confirmaEmail}
                  onChange={(event) => setConfirmaEmail(event.target.value)}
                  maxLength={255}
                />
              </label>
              <label>
                <span>NOVA SENHA</span>
                <input
                  aria-label="Nova senha"
                  type="password"
                  placeholder="Deixe vazio para manter"
                  value={novaSenha}
                  onChange={(event) => setNovaSenha(event.target.value)}
                  maxLength={72}
                  autoComplete="new-password"
                />
              </label>
              <label>
                <span>CONFIRMAR SENHA</span>
                <input
                  aria-label="Confirmar senha"
                  type="password"
                  placeholder="Confirme sua nova senha"
                  value={confirmaSenha}
                  onChange={(event) => setConfirmaSenha(event.target.value)}
                  maxLength={72}
                  autoComplete="new-password"
                />
              </label>
            </div>

            <h2>SENHA ATUAL</h2>
            <p className={styles.hint}>Obrigatória só para trocar email ou senha.</p>
            <input
              aria-label="Senha atual"
              type="password"
              placeholder="Digite sua senha atual"
              value={senhaAtual}
              onChange={(event) => setSenhaAtual(event.target.value)}
              maxLength={255}
              autoComplete="current-password"
            />
          </div>

          <button className={styles.save} type="submit" disabled={salvando} aria-busy={salvando}>
            {salvando ? 'SALVANDO...' : 'SALVAR'}
          </button>
          {feedback && (
            <p
              className={`${styles.feedback} ${feedback.tipo === 'erro' ? styles.feedbackErro : ''}`}
              role={feedback.tipo === 'erro' ? 'alert' : 'status'}
            >
              {feedback.texto}
            </p>
          )}
        </form>
      </section>
    </main>
  )
}
