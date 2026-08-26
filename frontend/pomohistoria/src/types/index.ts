/**
 * Tipos e interfaces compartilhadas para formulários
 * Reutilizáveis em toda a aplicação
 */

/** Interface para usuário após cadastro */
export interface User {
  id: string;
  nome: string;
  email: string;
  dataCriacao: Date;
  ativo: boolean;
}

/** Interface para requisição de cadastro */
export interface RegisterRequest {
  nome: string;
  email: string;
  senha: string;
}

/** Interface para resposta de cadastro */
export interface RegisterResponse {
  sucesso: boolean;
  mensagem: string;
  usuario?: User;
  token?: string;
}

/** Interface para erros de formulário */
export interface FormError {
  campo: string;
  mensagem: string;
  tipo: 'validacao' | 'servidor' | 'desconhecido';
}

/** Interface para estado de carregamento */
export interface LoadingState {
  isLoading: boolean;
  progress?: number;
  mensagem?: string;
}

/** Interface para estado de sucesso */
export interface SuccessState {
  sucesso: boolean;
  mensagem: string;
  timestamp: Date;
}

/** Tipos de validação */
export type ValidationRule = 'required' | 'email' | 'minLength' | 'maxLength' | 'pattern' | 'custom';

/** Configuração de validação */
export interface ValidationConfig {
  regra: ValidationRule;
  valor?: string | number;
  mensagem: string;
  validador?: (valor: any) => boolean;
}

/** Configuração de campo de formulário */
export interface FormFieldConfig {
  nome: string;
  label: string;
  tipo: 'text' | 'email' | 'password' | 'tel' | 'date' | 'textarea' | 'select';
  placeholder?: string;
  requerido: boolean;
  validacoes: ValidationConfig[];
  valor?: string;
  opcoes?: Array<{ label: string; valor: string }>;
  helperText?: string;
}

/** Configuração completa de formulário */
export interface FormConfig {
  id: string;
  titulo: string;
  descricao?: string;
  campos: FormFieldConfig[];
  botaoEnvio: {
    texto: string;
    textoCarregando?: string;
    cor?: string;
  };
  redirecionamentoPosEnvio?: string;
}

/** Estado completo de um formulário */
export interface FormState<T extends Record<string, any>> {
  valores: T;
  erros: Partial<Record<keyof T, string>>;
  tocados: Partial<Record<keyof T, boolean>>;
  isLoading: boolean;
  mensagemSucesso: string;
  mensagemErro: string;
}

/** Resposta de validação */
export interface ValidationResponse {
  valido: boolean;
  erros: FormError[];
  avisos?: string[];
}

/** Contexto de autenticação */
export interface AuthContext {
  usuarioAtual: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  erro: string | null;
  autenticar: (email: string, senha: string) => Promise<void>;
  registrar: (dados: RegisterRequest) => Promise<void>;
  logout: () => void;
  limparErro: () => void;
}

/** Opções de requisição */
export interface RequestOptions {
  method: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH';
  headers?: Record<string, string>;
  body?: any;
  timeout?: number;
  retry?: number;
  cacheTime?: number;
}

/** Resposta de requisição */
export interface ApiResponse<T> {
  sucesso: boolean;
  dados?: T;
  erro?: {
    codigo: string;
    mensagem: string;
    detalhes?: Record<string, any>;
  };
  timestamp: Date;
}

/** Tipos de notificação */
export type NotificationType = 'sucesso' | 'erro' | 'aviso' | 'info';

/** Interface para notificação */
export interface Notificacao {
  id: string;
  tipo: NotificationType;
  titulo: string;
  mensagem: string;
  duracao?: number;
  acao?: {
    texto: string;
    callback: () => void;
  };
}

/** Configuração de tema */
export interface TemaConfig {
  cores: {
    primaria: string;
    secundaria: string;
    sucesso: string;
    erro: string;
    aviso: string;
    info: string;
    fundo: string;
    texto: string;
  };
  tipografia: {
    fonteFamilia: string;
    tamanhoBase: number;
    escalaTipografia: number;
  };
  espacamento: {
    pequeno: string;
    medio: string;
    grande: string;
  };
}
