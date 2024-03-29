import axios, { type AxiosError, type AxiosResponse } from 'axios'
import type { Data } from '@/type/entity'
import { checkAccessToken, clearLoginState } from '@/utils/tools'
import router from '@/router'
import { loginStateStore } from '@/stores/store'

const http = axios.create({
  baseURL: import.meta.env.VITE_BASE_URL,
  timeout: 10000
})

http.interceptors.request.use(async config => {
  if (config.url !== '/token/refresh' && loginStateStore().login) {
    const token = await checkAccessToken()
    config.headers.Authorization = token
  }
  return config
})

http.interceptors.response.use((resp: AxiosResponse<Data<any>, any>): Promise<any> => {
  const data = resp.data
  if (resp.status === 200) {
    return Promise.resolve(data)
  } else {
    if (resp.status === 401) {
      clearLoginState()
      router.push({
        name: 'login'
      })
    }
    ElNotification.error({
      title: 'request forbidden',
      message: data.msg,
      showClose: true
    })
    return Promise.reject(resp)
  }
}, (error: AxiosError<any, any>) => {
  ElNotification.error({
    title: error.code,
    message: error.response?.data.msg ? error.response?.data.msg : error.message,
    showClose: true,
  })
  if (error.response?.status === 401) {
    clearLoginState()
    router.push({
      name: 'login'
    })
    if (router.currentRoute.value.fullPath === 'blogs') location.reload()
  }
  return Promise.reject(error)
})

export default http