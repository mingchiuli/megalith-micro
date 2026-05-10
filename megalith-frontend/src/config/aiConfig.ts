/**
 * AI Model Configuration
 * Externalized for environment-specific settings
 */

export const AI_MODELS = {
  // Image generation model (from environment variable)
  IMAGE_MODEL: import.meta.env.VITE_AI_IMAGE_MODEL || 'x/flux2-klein:9b-bf16',
  // Text generation model (from environment variable, optional)
  TEXT_MODEL: import.meta.env.VITE_AI_TEXT_MODEL || ''
}