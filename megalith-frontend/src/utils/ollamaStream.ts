/**
 * Ollama 流式请求工具
 *
 * 封装 NDJSON 流式解析逻辑，业务代码只需传入 onChunk 回调即可。
 * 支持文本生成（response + thinking 字段）和图片生成（image + completed/total 进度）。
 */

export interface StreamChunk {
  model: string
  response?: string // 文本生成的片段
  thinking?: string // 模型的思考过程（DeepSeek-R1 等模型支持）
  image?: string // 图片生成的 base64 片段
  completed?: number // 图片生成已完成步数
  total?: number // 图片生成总步数
  done: boolean
}

export type ThinkOption = boolean | 'low' | 'medium' | 'high'
export type ResponseFormat = 'json' | Record<string, unknown>

export interface StreamOptions {
  /** 完整 API 地址 */
  url: string
  /** 模型标识符 */
  model: string
  /** 提示词 */
  prompt: string
  /** 是否使用流式，默认 true */
  stream?: boolean
  /** 开启思考输出；GPT-OSS 使用 low/medium/high */
  think?: ThinkOption
  /** 强制模型使用 JSON 或指定的 JSON Schema 输出 */
  format?: ResponseFormat
  /** 每个 NDJSON chunk 的回调 */
  onChunk: (chunk: StreamChunk) => void
  /** 流结束回调 */
  onDone?: () => void
  /** 错误回调 */
  onError?: (e: unknown) => void
  /** 中断信号 */
  signal?: AbortSignal
}

/**
 * 发起 Ollama 流式请求，逐行解析 NDJSON，通过 onChunk 回调抛出每一帧数据。
 *
 * @example
 * ```ts
 * await ollamaStreamRequest({
 *   url: 'http://localhost:11434/api/generate',
 *   model: 'deepseek-r1',
 *   prompt: '你好',
 *   onChunk: (chunk) => {
 *     if (chunk.thinking) console.log('思考:', chunk.thinking)
 *     if (chunk.response) console.log('回复:', chunk.response)
 *   },
 *   onDone: () => console.log('完成')
 * })
 * ```
 */
export async function ollamaStreamRequest(options: StreamOptions): Promise<void> {
  const {
    url,
    model,
    prompt,
    stream = true,
    think,
    format,
    onChunk,
    onDone,
    onError,
    signal
  } = options

  try {
    const response = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        model,
        prompt,
        stream,
        ...(think !== undefined ? { think } : {}),
        ...(format !== undefined ? { format } : {}),
        options: { echo: false }
      }),
      signal
    })

    if (!response.ok) {
      throw new Error(`Ollama request failed with status ${response.status}`)
    }

    const reader = response.body?.getReader()
    if (!reader) {
      throw new Error('Ollama response body is empty')
    }

    const decoder = new TextDecoder()
    let buffer = ''
    let generationDone = false

    const emitLine = (line: string) => {
      if (!line.trim()) return false
      const chunk: StreamChunk = JSON.parse(line)
      onChunk(chunk)
      return chunk.done
    }

    try {
      while (true) {
        const { done, value } = await reader.read()
        if (done) {
          buffer += decoder.decode()
          if (buffer.trim()) emitLine(buffer)
          break
        }

        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split('\n')
        buffer = lines.pop() || ''

        for (const line of lines) {
          if (emitLine(line)) {
            generationDone = true
            break
          }
        }

        if (generationDone) {
          await reader.cancel()
          break
        }
      }
    } finally {
      reader.releaseLock()
    }
  } catch (e) {
    onError?.(e)
    throw e
  } finally {
    onDone?.()
  }
}

/**
 * 非流式 Ollama 请求，直接返回完整的 response 文本。
 *
 * @example
 * ```ts
 * const text = await ollamaRequest('http://localhost:11434/api/generate', 'qwen', '你好')
 * ```
 */
export async function ollamaRequest(
  url: string,
  model: string,
  prompt: string,
  options: Pick<StreamOptions, 'think' | 'format' | 'signal'> = {}
): Promise<string> {
  const response = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      model,
      prompt,
      stream: false,
      ...(options.think !== undefined ? { think: options.think } : {}),
      ...(options.format !== undefined ? { format: options.format } : {})
    }),
    signal: options.signal
  })
  if (!response.ok) {
    throw new Error(`Ollama request failed with status ${response.status}`)
  }
  const json = await response.json()
  if (typeof json.response !== 'string' || !json.response) {
    throw new Error('Ollama response text is empty')
  }
  return json.response
}
