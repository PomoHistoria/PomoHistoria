import { useState, type ChangeEvent } from 'react';
import { type SyntheticEvent } from "react";
import styles from './Cadastro.module.css';

interface FormData {
  nome: string;
  email: string;
  senha: string;
  confirmaSenha: string;
}

interface FormErrors {
  nome?: string;
  email?: string;
  senha?: string;
  confirmaSenha?: string;
  geral?: string;
}

export default function Cadastro() {
  const [formData, setFormData] = useState<FormData>({
    nome: '',
    email: '',
    senha: '',
    confirmaSenha: '',
  });

  const [errors, setErrors] = useState<FormErrors>({});
  const [isLoading, setIsLoading] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');

  const validateFormLocally = (): boolean => {
    const newErrors: FormErrors = {};

    // Validação do nome
    if (!formData.nome.trim()) {
      newErrors.nome = 'Nome é obrigatório';
    } else if (formData.nome.trim().length < 3) {
      newErrors.nome = 'Nome deve ter no mínimo 3 caracteres';
    }

    // Validação do email
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!formData.email.trim()) {
      newErrors.email = 'Email é obrigatório';
    } else if (!emailRegex.test(formData.email)) {
      newErrors.email = 'Email inválido';
    }

    // Validação da senha
    if (!formData.senha) {
      newErrors.senha = 'Senha é obrigatória';
    } else if (formData.senha.length < 6) {
      newErrors.senha = 'Senha deve ter no mínimo 6 caracteres';
    }

    // Validação da confirmação de senha
    if (!formData.confirmaSenha) {
      newErrors.confirmaSenha = 'Confirmação de senha é obrigatória';
    } else if (formData.senha !== formData.confirmaSenha) {
      newErrors.confirmaSenha = 'As senhas não coincidem';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prevState) => ({
      ...prevState,
      [name]: value,
    }));

    // Limpar erro do campo quando o usuário começa a digitar
    if (errors[name as keyof FormErrors]) {
      setErrors((prevErrors) => ({
        ...prevErrors,
        [name]: undefined,
      }));
    }
  };

  const handleSubmit = async (e: SyntheticEvent<HTMLFormElement>) => {
    e.preventDefault();

    if (!validateFormLocally()) {
      return;
    }

    setIsLoading(true);
    setSuccessMessage('');

    try { 
      const response = await fetch(
        "http://localhost:8080/api/auth/register", // URL do backend
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            nome: formData.nome.trim(),
            email: formData.email.trim().toLowerCase(),
            senha: formData.senha,
          }),
        },
      );

      if (!response.ok) {
        const errorData = await response.json();

        // Backend pode retornar múltiplos erros
        if (errorData.errors) {
          // Formato: { errors: { email: "Email já existe", nome: "..." } }
          setErrors(errorData.errors);
        } else if (errorData.message) {
          // Formato: { message: "Erro ao registrar" }
          setErrors({ geral: errorData.message });
        } else {
          setErrors({ geral: "Erro ao realizar cadastro" });
        }
        return;
      }

      const data = await response.json();
      setSuccessMessage("✅ Cadastro realizado com sucesso!");

      localStorage.setItem("token", data.token);

      setFormData({
        nome: '',
        email: '',
        senha: '',
        confirmaSenha: '',
      });

      // Redirecionar para login após 2 segundos
      setTimeout(() => {
        window.location.href = '/login';
      }, 2000);
    } catch (error) {
      console.error("Erro:", error);
      setErrors({
        geral:
          error instanceof Error
            ? error.message
            : "Erro ao realizar o cadastro",
      });
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className={styles.container}>
      {/* Seção de imagem/arte - visível apenas em desktop */}
      <div className={styles.imageSection}>
        <div className={styles.imagePlaceholder}>
          <img
            src="/public/fundo-art.png"
            alt="PomoHistória"
            className={styles.backgroundImage}
          />
        </div>
      </div>

      {/* Seção do formulário */}
      <div className={styles.formSection}>
        <div className={styles.formContainer}>
          <h1 className={styles.title}>CADASTRO</h1>

          {/* Erro geral (vindo do backend) */}
          {errors.geral && (
            <div className={styles.errorMessage} role="alert">
              ❌ {errors.geral}
            </div>
          )}

          {successMessage && (
            <div className={styles.successMessage} role="status">
              {successMessage}
            </div>
          )}

          <form onSubmit={handleSubmit} className={styles.form} noValidate>
            {/* Campo Nome */}
            <div className={styles.formGroup}>
              <input
                type="text"
                id="nome"
                name="nome"
                placeholder="Nome"
                value={formData.nome}
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

            {/* Campo Email */}
            <div className={styles.formGroup}>
              <input
                type="email"
                id="email"
                name="email"
                placeholder="Email"
                value={formData.email}
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

            {/* Campo Senha */}
            <div className={styles.formGroup}>
              <input
                type="password"
                id="senha"
                name="senha"
                placeholder="Senha"
                value={formData.senha}
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
            </div>

            {/* Campo Confirmar Senha */}
            <div className={styles.formGroup}>
              <input
                type="password"
                id="confirmaSenha"
                name="confirmaSenha"
                placeholder="Confirme sua senha"
                value={formData.confirmaSenha}
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

            {/* Botão Criar */}
            <button
              type="submit"
              disabled={isLoading}
              className={styles.submitButton}
              aria-busy={isLoading}
            >
              {isLoading ? "Criando..." : "CRIAR"}
            </button>
          </form>

          {/* Link para login */}
          <p className={styles.loginLink}>
            Já tem conta? <a href="/login">Faça login aqui</a>
          </p>
        </div>
      </div>
    </div>
  );
}
