/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_BASE_URL?: string
  readonly VITE_USE_MOCK_FALLBACK?: string
  readonly VITE_DEV_LOGGED_IN?: string
  readonly VITE_DEV_USER_ID?: string
  readonly VITE_DEV_USER_NAME?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
