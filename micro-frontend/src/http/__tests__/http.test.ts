import { describe, expect, it, vi } from 'vitest'
import { createApiClient } from '@/http/http'

const response = <T>(data: T) => Promise.resolve({ data: { data } })

describe('createApiClient', () => {
  it('sends DELETE parameters in the JSON request body', async () => {
    const deleteRequest = vi.fn(() => response(null))
    const api = createApiClient({
      httpClient: { delete: deleteRequest } as never,
      longHttpClient: {} as never,
      aiHttpClient: {} as never
    })

    await api.DELETE('/image', { url: 'https://cdn/image.png' })

    expect(deleteRequest).toHaveBeenCalledWith('/image', {
      data: { url: 'https://cdn/image.png' }
    })
  })

  it('unwraps POST responses', async () => {
    const postRequest = vi.fn(() => response('saved'))
    const api = createApiClient({
      httpClient: { post: postRequest } as never,
      longHttpClient: {} as never,
      aiHttpClient: {} as never
    })

    await expect(api.POST('/save', { id: 1 })).resolves.toBe('saved')
    expect(postRequest).toHaveBeenCalledWith('/save', { id: 1 })
  })

  it('tracks download and upload progress', async () => {
    vi.useFakeTimers()
    const download = vi.fn(
      (
        _url: string,
        config: {
          onDownloadProgress: (event: { loaded: number; total: number }) => void
        }
      ) => {
        config.onDownloadProgress({ loaded: 5, total: 10 })
        return Promise.resolve({ data: 'sql' })
      }
    )
    const upload = vi.fn(
      (
        _url: string,
        _body: FormData,
        config: {
          onUploadProgress: (event: { loaded: number; total: number }) => void
        }
      ) => {
        config.onUploadProgress({ loaded: 3, total: 4 })
        return response('image-url')
      }
    )
    const api = createApiClient({
      httpClient: {} as never,
      longHttpClient: { get: download, post: upload } as never,
      aiHttpClient: {} as never
    })
    const percentage = ref(0)
    const visible = ref(false)

    await expect(api.DOWNLOAD('/download', percentage, visible)).resolves.toEqual({ data: 'sql' })
    expect(percentage.value).toBe(50)
    expect(visible.value).toBe(true)
    vi.advanceTimersByTime(500)
    expect(visible.value).toBe(false)

    await expect(api.UPLOAD('/upload', new FormData(), percentage, visible)).resolves.toBe(
      'image-url'
    )
    expect(percentage.value).toBe(75)
    vi.advanceTimersByTime(500)
    expect(visible.value).toBe(false)
    vi.useRealTimers()
  })

  it('uses zero progress when a response has no content length', async () => {
    vi.useFakeTimers()
    const download = vi.fn(
      (_url: string, config: { onDownloadProgress: (event: { loaded: number }) => void }) => {
        config.onDownloadProgress({ loaded: 5 })
        return Promise.resolve({ data: 'sql' })
      }
    )
    const api = createApiClient({
      httpClient: {} as never,
      longHttpClient: { get: download } as never,
      aiHttpClient: {} as never
    })
    const percentage = ref(100)
    const visible = ref(false)

    await api.DOWNLOAD('/download', percentage, visible)

    expect(percentage.value).toBe(0)
    vi.runAllTimers()
    vi.useRealTimers()
  })

  it('forwards AbortSignal for cancellable GET requests', async () => {
    const getRequest = vi.fn(() => response({ content: [] }))
    const api = createApiClient({
      httpClient: { get: getRequest } as never,
      longHttpClient: {} as never,
      aiHttpClient: {} as never
    })
    const controller = new AbortController()

    await api.GET('/search', { signal: controller.signal })

    expect(getRequest).toHaveBeenCalledWith('/search', { signal: controller.signal })
  })
})
