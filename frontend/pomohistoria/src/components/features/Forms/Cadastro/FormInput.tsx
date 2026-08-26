import { type InputHTMLAttributes, type ChangeEvent } from 'react';
import styles from './FormInput.module.css';

interface FormInputProps extends InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  helperText?: string;
}

export default function FormInput({
  label,
  error,
  helperText,
  className,
  id,
  ...props
}: FormInputProps) {
  return (
    <div className={styles.formGroup}>
      {label && <label htmlFor={id} className={styles.label}>{label}</label>}
      
      <input
        id={id}
        className={`${styles.input} ${error ? styles.inputError : ''} ${className || ''}`}
        aria-invalid={!!error}
        aria-describedby={error ? `${id}-error` : helperText ? `${id}-helper` : undefined}
        {...props}
      />
      
      {error && (
        <span className={styles.errorMessage} id={`${id}-error`} role="alert">
          {error}
        </span>
      )}
      
      {helperText && !error && (
        <span className={styles.helperText} id={`${id}-helper`}>
          {helperText}
        </span>
      )}
    </div>
  );
}
