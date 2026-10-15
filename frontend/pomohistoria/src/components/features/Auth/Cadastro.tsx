import FundoForm from '../../../assets/FundoForm (1).png';
import styles from '../../Modal/Form.module.css';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../../hooks/useAuth';
import {
  getPasswordStrengthColor,
  getPasswordStrengthMessage,
  validateEmail,
  validateName,
  validatePassword,
  validatePasswordStrength,
} from '../../../utils/validation';
import type { RegisterRequest } from '../../../types';

interface CadastroForm extends RegisterRequest {
  confirmaSenha: string;
}

const initialValues: CadastroForm = {
  nome: '',
  email: '',
  senha: '',
  confirmaSenha: '',
};

export default function Cadastro() {
  const navigate = useNavigate();
  const {
    values,
    errors,
    isLoading,
    successMessage,
    handleChange,
    handleSubmit,
  } = useAuth<CadastroForm>({
    initialValues,
    validate: ({ nome, email, senha, confirmaSenha }) => {
      const validationErrors: Partial<Record<keyof CadastroForm, string>> = {};

      if (!nome.trim()) validationErrors.nome = 'Nome é obrigatório';
      else if (!validateName(nome)) validationErrors.nome = 'Nome deve ter no mínimo 3 caracteres';

      if (!email.trim()) validationErrors.email = 'Email é obrigatório';
      else if (!validateEmail(email.trim())) validationErrors.email = 'Email inválido';

      if (!senha) validationErrors.senha = 'Senha é obrigatória';
      else if (!validatePassword(senha)) validationErrors.senha = 'Senha deve ter no mínimo 6 caracteres';

      if (!confirmaSenha) validationErrors.confirmaSenha = 'Confirmação de senha é obrigatória';
      else if (senha !== confirmaSenha) validationErrors.confirmaSenha = 'As senhas não coincidem';

      return validationErrors;
    },
    onSubmit: async ({ nome, email, senha }) => {
      const response = await fetch('http://localhost:8080/api/auth/register', {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            nome: nome.trim(),
            email: email.trim().toLowerCase(),
            senha,
          }),
        });

      if (!response.ok) {
        const errorData = await response.json().catch(() => null) as { message?: string } | null;
        throw new Error(errorData?.message ?? 'Erro ao realizar cadastro');
      }

      const data = await response.json() as { token?: string };
      if (data.token) localStorage.setItem('token', data.token);
      window.setTimeout(() => {
        navigate('/login');
      }, 2000);
    },
  });

  return (
    <div className={styles.container}>
      {/* Seção de imagem/arte - visível apenas em desktop */}
      <div className={styles.imageSection}>
        <figure className={styles.imageFigure}>
          <img
            src={FundoForm}
            alt="PomoHistória"
            className={styles.backgroundImage}
          />
        </figure>
      </div>

      {/* Seção do formulário */}
      <div className={styles.formSection}>
        <div className={styles.formContainer}>
          <h1 className={styles.title}>CADASTRO</h1>

          {errors.geral && (
            <div className={styles.errorMessage} role="alert">
              {errors.geral}
            </div>
          )}

          {successMessage && (
            <div className={styles.successMessage} role="status">
              {successMessage}
            </div>
          )}

          <form onSubmit={handleSubmit} className={styles.form} noValidate>
            <div className={styles.formGroup}>
              <input
                type="text"
                id="nome"
                name="nome"
                placeholder="Nome"
                value={values.nome}
                onChange={handleChange}
                disabled={isLoading}
                className={`${styles.input} ${errors.nome ? styles.inputError : ""}`}
                aria-label="Nome completo"
                aria-invalid={!!errors.nome}
                aria-describedby={errors.nome ? "erro-nome" : undefined}
              />
              {errors.nome && (
                <span className={styles.errorMessage} id="erro-nome">
                  {errors.nome}
                </span>
              )}
            </div>

            <div className={styles.formGroup}>
              <input
                type="email"
                id="email"
                name="email"
                placeholder="Email"
                value={values.email}
                onChange={handleChange}
                disabled={isLoading}
                className={`${styles.input} ${errors.email ? styles.inputError : ""}`}
                aria-label="Email"
                aria-invalid={!!errors.email}
                aria-describedby={errors.email ? "erro-email" : undefined}
              />
              {errors.email && (
                <span className={styles.errorMessage} id="erro-email">
                  {errors.email}
                </span>
              )}
            </div>

            <div className={styles.formGroup}>
              <input
                type="password"
                id="senha"
                name="senha"
                placeholder="Senha"
                value={values.senha}
                onChange={handleChange}
                disabled={isLoading}
                className={`${styles.input} ${errors.senha ? styles.inputError : ""}`}
                aria-label="Senha"
                aria-invalid={!!errors.senha}
                aria-describedby={errors.senha ? "erro-senha" : undefined}
              />
              {errors.senha && (
                <span className={styles.errorMessage} id="erro-senha">
                  {errors.senha}
                </span>
              )}
              {values.senha && !errors.senha && (
                <span
                  className={styles.passwordStrength}
                  style={{ color: getPasswordStrengthColor(validatePasswordStrength(values.senha)) }}
                >
                  {getPasswordStrengthMessage(validatePasswordStrength(values.senha))}
                </span>
              )}
            </div>

            <div className={styles.formGroup}>
              <input
                type="password"
                id="confirmaSenha"
                name="confirmaSenha"
                placeholder="Confirme sua senha"
                value={values.confirmaSenha}
                onChange={handleChange}
                disabled={isLoading}
                className={`${styles.input} ${errors.confirmaSenha ? styles.inputError : ""}`}
                aria-label="Confirmação de senha"
                aria-invalid={!!errors.confirmaSenha}
                aria-describedby={
                  errors.confirmaSenha ? "erro-confirmaSenha" : undefined
                }
              />
              {errors.confirmaSenha && (
                <span className={styles.errorMessage} id="erro-confirmaSenha">
                  {errors.confirmaSenha}
                </span>
              )}
            </div>

            <button
              type="submit"
              disabled={isLoading}
              className={styles.submitButton}
              aria-busy={isLoading}
            >
              {isLoading ? "Criando..." : "CRIAR"}
            </button>
          </form>

          <p className={styles.loginLink}>
            Já tem conta? <Link to="/login">Faça login aqui</Link>
          </p>
        </div>
      </div>
    </div>
  );
}
