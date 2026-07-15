import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ollamaStreamRequest, type StreamChunk } from '@/utils/ollamaStream'

const streamResponse = (chunks: StreamChunk[]) => {
  const encoder = new TextEncoder()
  const body = new ReadableStream({
    start(controller) {
      const ndjson = chunks.map((chunk) => JSON.stringify(chunk)).join('\n') + '\n'
      controller.enqueue(encoder.encode(ndjson))
      controller.close()
    }
  })
  return new Response(body, { status: 200 })
}

describe('ollamaStreamRequest', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
  })

  it('enables thinking and emits thinking chunks', async () => {
    const chunks: StreamChunk[] = [
      { model: 'gemma4', thinking: '分析文章', done: false },
      { model: 'gemma4', response: '{"title":"标题"}', done: true }
    ]
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue(streamResponse(chunks))
    const onChunk = vi.fn()

    await ollamaStreamRequest({
      url: 'http://localhost:11434/api/generate',
      model: 'gemma4',
      prompt: '生成标题',
      think: true,
      onChunk
    })

    const request = fetchMock.mock.calls[0]?.[1]
    expect(JSON.parse(request?.body as string)).toMatchObject({
      model: 'gemma4',
      prompt: '生成标题',
      stream: true,
      think: true
    })
    expect(onChunk).toHaveBeenNthCalledWith(1, chunks[0])
    expect(onChunk).toHaveBeenNthCalledWith(2, chunks[1])
  })

  it('finishes when Ollama sends done without closing the connection', async () => {
    const encoder = new TextEncoder()
    const cancel = vi.fn()
    const body = new ReadableStream({
      start(controller) {
        controller.enqueue(
          encoder.encode(`${JSON.stringify({ model: 'gemma4', response: '{}', done: true })}\n`)
        )
      },
      cancel
    })
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(new Response(body, { status: 200 }))

    await ollamaStreamRequest({
      url: 'http://localhost:11434/api/generate',
      model: 'gemma4',
      prompt: '生成标题',
      onChunk: vi.fn()
    })

    expect(cancel).toHaveBeenCalledOnce()
  })

  it('throws request errors and calls lifecycle callbacks once', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(new Response(null, { status: 500 }))
    const onDone = vi.fn()
    const onError = vi.fn()

    await expect(
      ollamaStreamRequest({
        url: 'http://localhost:11434/api/generate',
        model: 'gemma4',
        prompt: '生成标题',
        onChunk: vi.fn(),
        onDone,
        onError
      })
    ).rejects.toThrow('status 500')

    expect(onError).toHaveBeenCalledOnce()
    expect(onDone).toHaveBeenCalledOnce()
  })

  it('parses the final NDJSON line without a trailing newline', async () => {
    const chunk: StreamChunk = { model: 'gemma4', response: '{}', done: true }
    const body = new ReadableStream({
      start(controller) {
        controller.enqueue(new TextEncoder().encode(JSON.stringify(chunk)))
        controller.close()
      }
    })
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(new Response(body, { status: 200 }))
    const onChunk = vi.fn()

    await ollamaStreamRequest({
      url: 'http://localhost:11434/api/generate',
      model: 'gemma4',
      prompt: '生成标题',
      onChunk
    })

    expect(onChunk).toHaveBeenCalledWith(chunk)
  })
})
