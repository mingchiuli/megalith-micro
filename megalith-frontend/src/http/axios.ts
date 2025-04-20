import axios, { type AxiosError, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import type { Data } from '@/type/entity'
import { checkAccessToken, clearLoginState } from '@/utils/tools'
import router from '@/router'
import { loginStateStore } from '@/stores/store'

const http = axios.create({
  baseURL: import.meta.env.VITE_BASE_URL,
  timeout: 10000
})

const longHttp = axios.create({
  baseURL: import.meta.env.VITE_BASE_URL
})

// 请求拦截器
const requestInterceptor = async (config: InternalAxiosRequestConfig) => {
  const url = config.url
  if (url !== '/token/refresh' && loginStateStore().login) {
    const accessToken = localStorage.getItem('accessToken')
    const token = await checkAccessToken(accessToken!)
    config.headers.Authorization = token
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
http.interceptors.request.use(requestInterceptor)
http.interceptors.response.use(responseInterceptor, errorInterceptor)
longHttp.interceptors.request.use(requestInterceptor)
longHttp.interceptors.response.use(responseInterceptor, errorInterceptor)

export { http, longHttp }
