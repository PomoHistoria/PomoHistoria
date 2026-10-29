/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** URL pública do backend, sem barra no final. */
  readonly VITE_API_BASE_URL?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
