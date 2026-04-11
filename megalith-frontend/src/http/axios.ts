import axios, {type AxiosError, type AxiosResponse, type InternalAxiosRequestConfig} from 'axios'
import { context } from '@opentelemetry/api'
import { W3CTraceContextPropagator } from '@opentelemetry/core'
import type {Data} from '@/type/entity'
import {clearLoginState, updateAccessToken} from '@/utils/tools'
import router from '@/router'
import {loginStateStore} from '@/stores/store'
import {API_CONFIG, API_ENDPOINTS} from '@/config/apiConfig'

const httpClient = axios.create({
  baseURL: API_CONFIG.BASE_URL,
  timeout: API_CONFIG.TIMEOUT
})

const longHttpClient = axios.create({
  baseURL: API_CONFIG.BASE_URL,
  timeout: API_CONFIG.LONG_TIMEOUT
})

const aiHttpClient = axios.create({
  baseURL: API_CONFIG.AI_BASE_URL,
  timeout: API_CONFIG.LONG_TIMEOUT

})

const propagator = new W3CTraceContextPropagator()

// 请求拦截器
const requestInterceptor = async (config: InternalAxiosRequestConfig) => {
  const url = config.url
  if (url !== API_ENDPOINTS.AUTH.TOKEN_REFRESH && loginStateStore().login) {
    config.headers.Authorization = await updateAccessToken()
  }
  // Inject trace context for non-Ollama requests
  if (!url?.startsWith(API_CONFIG.AI_BASE_URL)) {
    propagator.inject(context.active(), config.headers, {
      req: { headers: config.headers }
    } as never)
  }
  return config
}

// 响应拦截器
const responseInterceptor = (response: AxiosResponse) => {
  if (response.status === 200) {
    return response
  }
  
  ElNotification.error({
    title: 'Request Error',
    message: response.data.msg,
    showClose: true
  })
  return Promise.reject(new Error(response.data.msg))
}

// 错误拦截器
const errorInterceptor = (error: AxiosError<Data<unknown>>) => {
  ElNotification.error({
    title: error.code,
    message: error.response?.data.msg ?? error.message,
    showClose: true
  })
  
  if (error.response?.status === 403) {
    clearLoginState()
    router.push({
      name: 'login'
    })
  }
  return Promise.reject(error)
}

// 应用拦截器
httpClient.interceptors.request.use(requestInterceptor)
httpClient.interceptors.response.use(responseInterceptor, errorInterceptor)
longHttpClient.interceptors.request.use(requestInterceptor)
longHttpClient.interceptors.response.use(responseInterceptor, errorInterceptor)

export { httpClient, longHttpClient, aiHttpClient }
