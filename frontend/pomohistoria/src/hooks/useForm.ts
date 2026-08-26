import { useState, useCallback, ChangeEvent, FormEvent } from 'react';

interface UseFormOptions<T> {
  initialValues: T;
  onSubmit: (values: T) => Promise<void> | void;
  validate?: (values: T) => Record<keyof T, string>;
}

interface UseFormReturn<T> {
  values: T;
  errors: Record<keyof T, string>;
  isLoading: boolean;
  successMessage: string;
  handleChange: (e: ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => void;
  handleSubmit: (e: FormEvent<HTMLFormElement>) => Promise<void>;
  resetForm: () => void;
  setFieldValue: (field: keyof T, value: any) => void;
  setErrors: (errors: Record<keyof T, string>) => void;
}

/**
 * Hook customizado para gerenciar estado de formulários
 * Fornece validação, loading state e gerenciamento de erros
 */
export function useForm<T extends Record<string, any>>(
  options: UseFormOptions<T>
): UseFormReturn<T> {
  const { initialValues, onSubmit, validate } = options;
  const [values, setValues] = useState<T>(initialValues);
  const [errors, setErrors] = useState<Record<keyof T, string>>({} as Record<keyof T, string>);
  const [isLoading, setIsLoading] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');

  const handleChange = useCallback((
    e: ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>
  ) => {
    const { name, value, type } = e.target;
    const fieldValue = type === 'checkbox' ? (e.target as HTMLInputElement).checked : value;

    setValues((prevState) => ({
      ...prevState,
      [name]: fieldValue,
    }));

    // Limpar erro ao digitar
    if (errors[name as keyof T]) {
      setErrors((prevErrors) => ({
        ...prevErrors,
        [name]: '',
      }));
    }
  }, [errors]);

  const handleSubmit = useCallback(
    async (e: FormEvent<HTMLFormElement>) => {
      e.preventDefault();

      if (validate) {
        const validationErrors = validate(values);
        if (Object.keys(validationErrors).length > 0) {
          setErrors(validationErrors);
          return;
        }
      }

      setIsLoading(true);
      setSuccessMessage('');

      try {
        await onSubmit(values);
        setSuccessMessage('Operação realizada com sucesso!');
        setValues(initialValues);
      } catch (error) {
        setErrors({
          ...errors,
          [Object.keys(initialValues)[0]]: error instanceof Error ? error.message : 'Erro ao processar formulário',
        } as Record<keyof T, string>);
      } finally {
        setIsLoading(false);
      }
    },
    [values, initialValues, validate, onSubmit, errors]
  );

  const resetForm = useCallback(() => {
    setValues(initialValues);
    setErrors({} as Record<keyof T, string>);
    setSuccessMessage('');
  }, [initialValues]);

  const setFieldValue = useCallback((field: keyof T, value: any) => {
    setValues((prevState) => ({
      ...prevState,
      [field]: value,
    }));
  }, []);

  return {
    values,
    errors,
    isLoading,
    successMessage,
    handleChange,
    handleSubmit,
    resetForm,
    setFieldValue,
    setErrors,
  };
}
