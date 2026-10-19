import FundoForm from '../../../assets/FundoForm (1).png';
import styles from '../../Modal/Form.module.css';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../../hooks/useAuth';
import { authApi } from '../../../services/authApi';
import { validateEmail, validatePassword } from '../../../utils/validation';
import type { LoginRequest } from '../../../types';

const initialValues: LoginRequest = {
  email: '',
  senha: '',
};

export default function Login() {
  const navigate = useNavigate();
  const {
    values,
    errors,
    isLoading,
    successMessage,
    handleChange,
    handleSubmit,
  } = useAuth<LoginRequest>({
    initialValues,
    validate: ({ email, senha }) => {
      const validationErrors: Partial<Record<keyof LoginRequest, string>> = {};

      if (!email.trim()) validationErrors.email = 'Email é obrigatório';
      else if (!validateEmail(email.trim())) validationErrors.email = 'Email inválido';

      if (!senha) validationErrors.senha = 'Senha é obrigatória';
      else if (!validatePassword(senha)) {
        validationErrors.senha = 'Senha deve ter no mínimo 6 caracteres';
      }

      return validationErrors;
    },
    onSubmit: async ({ email, senha }) => {
      await authApi.entrar({
        email: email.trim().toLowerCase(),
        senha,
      });
      navigate('/');
    },
  });

  return (
    <div className={styles.container}>
      <div className={styles.imageSection}>
        <figure className={styles.imageFigure}>
          <img
            src={FundoForm}
            alt="PomoHistória"
            className={styles.backgroundImage}
          />
        </figure>
      </div>

      <div className={styles.formSection}>
        <div className={styles.formContainer}>
          <h1 className={styles.title}>LOGIN</h1>

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
                type="email"
                id="email"
                name="email"
                placeholder="Email"
                value={values.email}
                onChange={handleChange}
                disabled={isLoading}
                className={`${styles.input} ${errors.email ? styles.inputError : ''}`}
                aria-label="Email"
                aria-invalid={!!errors.email}
                aria-describedby={errors.email ? 'erro-email' : undefined}
                autoComplete="email"
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
                className={`${styles.input} ${errors.senha ? styles.inputError : ''}`}
                aria-label="Senha"
                aria-invalid={!!errors.senha}
                aria-describedby={errors.senha ? 'erro-senha' : undefined}
                autoComplete="current-password"
              />
              {errors.senha && (
                <span className={styles.errorMessage} id="erro-senha">
                  {errors.senha}
                </span>
              )}
            </div>

            <button
              type="submit"
              disabled={isLoading}
              className={styles.submitButton}
              aria-busy={isLoading}
            >
              {isLoading ? 'Entrando...' : 'ENTRAR'}
            </button>
          </form>

          <p className={styles.loginLink}>
            Ainda não tem conta? <Link to="/cadastro">Cadastre-se aqui</Link>
          </p>
        </div>
      </div>
    </div>
  );
}
