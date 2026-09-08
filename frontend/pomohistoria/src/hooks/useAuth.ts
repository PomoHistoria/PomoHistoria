import { useState, useCallback, type ChangeEvent, type SubmitEvent } from 'react';

interface UseAuthOptions<FormValuesOptions> {
  initialValues: FormValuesOptions;
  onSubmit: (values: FormValuesOptions) => Promise<void> | void;
  validate?: (values: FormValuesOptions) => Partial<Record<keyof FormValuesOptions, string>>;
}

type FormErrors<FormValues> = Partial<Record<keyof FormValues, string>> & {
  geral?: string;
};

interface UseAuthReturn<FormValues> {
  values: FormValues;
  errors: FormErrors<FormValues>;
  isLoading: boolean;
  successMessage: string;
  handleChange: (e: ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => void;
  handleSubmit: (e: SubmitEvent<HTMLFormElement>) => Promise<void>;
  resetForm: () => void;
  setFieldValue: (field: keyof FormValues, value: FormValues[keyof FormValues]) => void;
  setErrors: (errors: FormErrors<FormValues>) => void;
}
export function useAuth<FormValues extends Record<string, any>>(
  options: UseAuthOptions<FormValues>
): UseAuthReturn<FormValues> {
  const { initialValues, onSubmit, validate } = options;
  const [values, setValues] = useState<FormValues>(initialValues);
  const [errors, setErrors] = useState<FormErrors<FormValues>>({});
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
    if (errors[name as keyof FormValues] || errors.geral) {
      setErrors((prevErrors) => ({
        ...prevErrors,
        [name]: '',
        geral: '',
      }));
    }
  }, [errors]);

  const handleSubmit = useCallback(
    async (e: SubmitEvent<HTMLFormElement>) => {
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
          geral: error instanceof Error ? error.message : 'Erro ao processar formulário',
        });
      } finally {
        setIsLoading(false);
      }
    },
    [values, initialValues, validate, onSubmit, errors]
  );

  const resetForm = useCallback(() => {
    setValues(initialValues);
    setErrors({});
    setSuccessMessage('');
  }, [initialValues]);

  const setFieldValue = useCallback((field: keyof FormValues, value: FormValues[keyof FormValues]) => {
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
