/// <reference types="element-plus/global" />

// Environment variable type definitions
interface ImportMetaEnv {
  readonly VITE_APP_NAME: string
  readonly VITE_BASE_URL: string
  readonly VITE_BASE_WS_URL: string
  readonly VITE_AI_BASE_URL: string
  readonly VITE_AI_IMAGE_MODEL: string
  readonly VITE_AI_TEXT_MODEL: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}