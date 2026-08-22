import { Ollama, type GenerateRequest, type GenerateResponse } from 'ollama/browser'
import { API_CONFIG } from '@/config/apiConfig'

export type GenerateChunk = Partial<GenerateResponse> &
  Pick<GenerateResponse, 'model' | 'done'> & {
    // Image models add progress data that is not yet included in ollama-js types.
    image?: string
    completed?: number
    total?: number
  }

const client = new Ollama({ host: API_CONFIG.AI_BASE_URL })

export const generate = (
  request: GenerateRequest & { stream: true }
): Promise<AsyncIterable<GenerateChunk>> => client.generate(request)
