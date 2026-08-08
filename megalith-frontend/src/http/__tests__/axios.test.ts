import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import axios, {
  AxiosError,
  AxiosHeaders,
  type AxiosAdapter,
  type AxiosRequestConfig,
  type AxiosResponse,
  type InternalAxiosRequestConfig
} from 'axios'
import { createHttpClients } from '@/http/axios'

const response = (
  config: AxiosRequestConfig,
  data: unknown = { data: null },
  headers: Record<string, string | string[]> = {}
): AxiosResponse => ({
  data,
  status: 200,
  statusText: 'OK',
  headers,
  config: config as InternalAxiosRequestConfig,
  request: {}
})

const unauthorized = (config: AxiosRequestConfig) => {
  const result = {
    ...response(config, { msg: 'unauthorized' }),
    status: 401,
    statusText: 'Unauthorized'
  }
  return new AxiosError(
    'Unauthorized',
    AxiosError.ERR_BAD_REQUEST,
    config as InternalAxiosRequestConfig,
    undefined,
    result
  )
}

describe('createHttpClients SSR context', () => {
  const originalAdapter = axios.defaults.adapter

  beforeEach(() => {
    vi.stubGlobal('window', undefined)
  })

  afterEach(() => {
    axios.defaults.adapter = originalAdapter
    vi.unstubAllGlobals()
    vi.restoreAllMocks()
  })

  it('forwards only the current request cookie and origin', async () => {
    const requests: InternalAxiosRequestConfig[] = []
    axios.defaults.adapter = vi.fn(async (config) => {
      requests.push(config)
      return response(config)
    }) as AxiosAdapter

    const first = createHttpClients({
      baseURL: 'http://gateway',
      cookie: 'megalith_access_token=first',
      origin: 'https://first.example'
    })
    const second = createHttpClients({
      baseURL: 'http://gateway',
      cookie: 'megalith_access_token=second',
      origin: 'https://second.example'
    })

    await Promise.all([first.httpClient.get('/one'), second.httpClient.get('/two')])

    const firstHeaders = AxiosHeaders.from(requests[0]!.headers)
    const secondHeaders = AxiosHeaders.from(requests[1]!.headers)
    expect(firstHeaders.get('Cookie')).toBe('megalith_access_token=first')
    expect(firstHeaders.get('Origin')).toBe('https://first.example')
    expect(secondHeaders.get('Cookie')).toBe('megalith_access_token=second')
    expect(secondHeaders.get('Origin')).toBe('https://second.example')
    expect(firstHeaders.get('traceparent')).toMatch(/^00-[\da-f]{32}-[\da-f]{16}-01$/)
  })

  it('refreshes once, retries with the new cookie, and exposes Set-Cookie', async () => {
    const setCookies = vi.fn()
    let protectedCalls = 0
    let refreshCalls = 0

    axios.defaults.adapter = vi.fn(async (config) => {
      if (config.url === '/token/refresh') {
        refreshCalls++
        return response(
          config,
          { data: { accessToken: 'legacy-body' } },
          {
            'set-cookie': ['megalith_access_token=new-token; Path=/; HttpOnly']
          }
        )
      }
      if (config.url === '/protected') {
        protectedCalls++
        if (protectedCalls <= 2) throw unauthorized(config)
        expect(AxiosHeaders.from(config.headers).get('Cookie')).toContain(
          'megalith_access_token=new-token'
        )
        return response(config, { data: { ok: true } })
      }
      throw new Error(`Unexpected URL: ${config.url}`)
    }) as AxiosAdapter

    const { httpClient } = createHttpClients({
      baseURL: 'http://gateway',
      cookie: 'megalith_access_token=expired; megalith_refresh_token=refresh',
      origin: 'https://chiu.wiki',
      onSetCookie: setCookies
    })

    const [first, second] = await Promise.all([
      httpClient.get('/protected'),
      httpClient.get('/protected')
    ])

    expect(first.data).toEqual({ data: { ok: true } })
    expect(second.data).toEqual({ data: { ok: true } })
    expect(refreshCalls).toBe(1)
    expect(protectedCalls).toBe(4)
    expect(setCookies).toHaveBeenCalledWith(['megalith_access_token=new-token; Path=/; HttpOnly'])
  })
})
