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

export interface StreamOptions {
  /** 完整 API 地址 */
  url: string
  /** 模型标识符 */
  model: string
  /** 提示词 */
  prompt: string
  /** 是否使用流式，默认 true */
  stream?: boolean
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
  const { url, model, prompt, stream = true, onChunk, onDone, onError, signal } = options

  try {
    const response = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        model,
        prompt,
        stream,
        options: { echo: false }
      }),
      signal
    })

    if (!response.ok) {
      onDone?.()
      return
    }

    const reader = response.body?.getReader()
    if (!reader) {
      onDone?.()
      return
    }

    const decoder = new TextDecoder()
    let buffer = ''

    try {
      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split('\n')
        buffer = lines.pop() || ''

        for (const line of lines) {
          if (!line.trim()) continue
          const chunk: StreamChunk = JSON.parse(line)
          onChunk(chunk)
        }
      }
    } finally {
      reader.releaseLock()
    }
  } catch (e) {
    onError?.(e)
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
export async function ollamaRequest(url: string, model: string, prompt: string): Promise<string> {
  try {
    const response = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ model, prompt, stream: false })
    })
    if (!response.ok) return ''
    const json = await response.json()
    return json.response || ''
  } catch {
    return ''
  }
}
