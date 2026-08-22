import axios, {
  AxiosError,
  type AxiosInstance,
  type AxiosResponse,
  type InternalAxiosRequestConfig
} from 'axios'
import type { Data } from '@/type/entity'
import { API_CONFIG, API_ENDPOINTS } from '@/config/apiConfig'

type HttpClientOptions = {
  baseURL?: string
  cookie?: string
  origin?: string
  onSetCookie?: (value: string[]) => void
  onUnauthorized?: () => void
  onError?: (error: AxiosError<Data<unknown>>) => void
}

export type HttpClients = {
  httpClient: AxiosInstance
  longHttpClient: AxiosInstance
  aiHttpClient: AxiosInstance
}

const setCookieValues = (headers: AxiosResponse['headers']): string[] => {
  const value = headers['set-cookie']
  if (!value) return []
  return Array.isArray(value) ? value : [value]
}

const replaceCookie = (source: string, setCookie: string): string => {
  const pair = setCookie.split(';', 1)[0]
  if (!pair) return source
  const [name] = pair.split('=', 1)
  const values = source
    .split(';')
    .map((value) => value.trim())
    .filter((value) => value && !value.startsWith(`${name}=`))
  values.push(pair)
  return values.join('; ')
}

export const createHttpClients = (options: HttpClientOptions = {}): HttpClients => {
  let cookie = options.cookie ?? ''
  let refreshPromise: Promise<void> | null = null
  const browser = typeof window !== 'undefined'
  const baseURL = options.baseURL ?? API_CONFIG.BASE_URL

  const common = {
    baseURL,
    withCredentials: true,
    timeout: API_CONFIG.TIMEOUT
  }
  const httpClient = axios.create(common)
  const longHttpClient = axios.create({ ...common, timeout: API_CONFIG.LONG_TIMEOUT })
  const aiHttpClient = axios.create({
    baseURL: API_CONFIG.AI_BASE_URL,
    timeout: API_CONFIG.LONG_TIMEOUT
  })
  const refreshClient = axios.create(common)

  const applyRequestContext = (config: InternalAxiosRequestConfig) => {
    if (!browser && cookie) config.headers.Cookie = cookie
    if (!browser && options.origin) config.headers.Origin = options.origin
    return config
  }

  const captureCookies = (response: AxiosResponse) => {
    const values = setCookieValues(response.headers)
    if (values.length) {
      values.forEach((value) => {
        cookie = replaceCookie(cookie, value)
      })
      options.onSetCookie?.(values)
    }
    return response
  }

  const refresh = async () => {
    if (!refreshPromise) {
      refreshPromise = refreshClient
        .post(API_ENDPOINTS.AUTH.TOKEN_REFRESH, null, {
          headers: {
            ...(cookie ? { Cookie: cookie } : {}),
            ...(options.origin ? { Origin: options.origin } : {})
          }
        })
        .then(captureCookies)
        .then(() => undefined)
        .finally(() => {
          refreshPromise = null
        })
    }
    return refreshPromise
  }

  const responseError = async (error: AxiosError<Data<unknown>>) => {
    const config = error.config as (InternalAxiosRequestConfig & { _retried?: boolean }) | undefined
    if (
      error.response?.status === 401 &&
      config &&
      !config._retried &&
      config.url !== API_ENDPOINTS.AUTH.TOKEN_REFRESH
    ) {
      config._retried = true
      try {
        await refresh()
        if (!browser && cookie) config.headers.Cookie = cookie
        return httpClient.request(config)
      } catch {
        options.onUnauthorized?.()
      }
    }

    options.onError?.(error)
    return Promise.reject(error)
  }

  ;[httpClient, longHttpClient].forEach((client) => {
    client.interceptors.request.use(applyRequestContext)
    client.interceptors.response.use(captureCookies, responseError)
  })

  return { httpClient, longHttpClient, aiHttpClient }
}
