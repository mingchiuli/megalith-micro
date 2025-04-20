import axios, {type InternalAxiosRequestConfig } from 'axios'
import { checkAccessToken } from '@/utils/tools'
import { loginStateStore } from '@/stores/store'

const http = axios.create({
  baseURL: import.meta.env.VITE_BASE_URL,
  timeout: 10000
})

const longHttp = axios.create({
  baseURL: import.meta.env.VITE_BASE_URL
})

const requestInterceptor = async (config: InternalAxiosRequestConfig) => {
  const url = config.url
  if (url !== '/token/refresh' && loginStateStore().login) {
    const accessToken = localStorage.getItem('accessToken')
    const token = await checkAccessToken(accessToken!)
    config.headers.Authorization = token
  }
  return config
}

http.interceptors.request.use(requestInterceptor)
longHttp.interceptors.request.use(requestInterceptor)

export { http, longHttp }