import axios, { type AxiosError, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import type { Data, JWTStruct, RefreshStruct } from '@/type/entity'
import { loginStateStore } from '@/stores/store'
import { storeToRefs } from 'pinia'
import router from '@/router'
import { Base64 } from 'js-base64'

const http = axios.create({
  baseURL: import.meta.env.VITE_BASE_URL,
  timeout: 10000
});

http.interceptors.request.use(async (config: InternalAxiosRequestConfig<any>) => {
  const accessToken: string | null = localStorage.getItem('accessToken')
  if (accessToken && config.url !== '/token/refresh') {
    const { login } = storeToRefs(loginStateStore())
    login.value = true
    let tokenArray: string[] = accessToken.split(".")
    const jwt: JWTStruct = JSON.parse(Base64.fromBase64(tokenArray[1]))

    const now: number = Math.floor(new Date().getTime() / 1000)
    //ten minutes
    if (jwt.exp - now < 600) {
      const refreshToken: string | null = localStorage.getItem('refreshToken')
      const data = await http.get<never, Data<RefreshStruct>>('/token/refresh', {
        headers: { Authorization: refreshToken }
      })
      const token = data.data.accessToken
      localStorage.setItem('accessToken', token)
    }
    config.headers.Authorization = localStorage.getItem('accessToken')
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
      showClose: true,
    })
    ElMessage.error(data.msg)
    return Promise.reject(resp)
  }
}, (error: AxiosError<any, any>) => {
  ElNotification.error({
    title: error.code,
    message: error.response?.data.msg ? error.response.data.msg : error.message,
    showClose: true,
  })
  if (error.response?.status === 401) {
    clearLoginState()
    router.push({
      name: 'login'
    })
  }
  return Promise.reject(error)
})

const clearLoginState = () => {
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
  const { login } = storeToRefs(loginStateStore())
  login.value = false
}

export default http