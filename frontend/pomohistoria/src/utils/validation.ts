export const validateEmail = (email: string): boolean => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

export const validatePassword = (password: string, minLength: number = 6): boolean => {
  return password.length >= minLength;
};

export const validatePasswordStrength = (password: string): 'weak' | 'medium' | 'strong' => {
  let strength: 'weak' | 'medium' | 'strong' = 'weak';

  if (password.length >= 8) {
    strength = 'medium';
  }

  if (
    password.length >= 12 &&
    /[a-z]/.test(password) &&
    /[A-Z]/.test(password) &&
    /[0-9]/.test(password) &&
    /[^a-zA-Z0-9]/.test(password)
  ) {
    strength = 'strong';
  }

  return strength;
};

export const getPasswordStrengthMessage = (
  strength: 'weak' | 'medium' | 'strong'
): string => {
  const messages = {
    weak: 'Senha fraca',
    medium: 'Senha moderada',
    strong: 'Senha forte',
  };
  return messages[strength];
};

export const getPasswordStrengthColor = (
  strength: 'weak' | 'medium' | 'strong'
): string => {
  const colors = {
    weak: '#ef4444',
    medium: '#f59e0b',
    strong: '#10b981',
  };
  return colors[strength];
};

export const validateName = (name: string, minLength: number = 3): boolean => {
  return name.trim().length >= minLength;
};