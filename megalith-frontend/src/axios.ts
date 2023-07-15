import axios, { type AxiosError, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import type { Data, JWTStruct, RefreshStruct } from '@/type/entity'
import { loginStateStore } from '@/stores/store'
import { storeToRefs } from 'pinia'
import router from '@/router'

axios.defaults.baseURL = 'http://127.0.0.1:8081'
axios.defaults.timeout = 10000

const convert = (token: string): JWTStruct => {
  return JSON.parse(atob(token))

}

axios.interceptors.request.use((config: InternalAxiosRequestConfig<any>) => {
  const accessToken: string | null = localStorage.getItem('accessToken')
  if (accessToken && config.url !== '/token/refresh') {
    const { login } = storeToRefs(loginStateStore())
    login.value = true
    let tokenArray: string[] = accessToken.split(".")
    const jwt: JWTStruct = convert(tokenArray[1])

    const now: number = Math.floor(new Date().getTime() / 1000)
    //ten minutes
    if (jwt.exp - now < 600) {
      const refreshToken: string | null = localStorage.getItem('refreshToken')
      axios.get('/token/refresh', {
        headers: { Authorization: refreshToken }
      }).then((resp: AxiosResponse<Data<RefreshStruct>>) => {
        const token = resp.data.data.accessToken
        localStorage.setItem('accessToken', token)
      })
    }
    config.headers.Authorization = localStorage.getItem('accessToken')
  }
  return config
})

axios.interceptors.response.use((resp: AxiosResponse<Data<any>, any>) => {
  const data: Data<any> = resp.data
  //@ts-ignore
  if (resp.status === 200) {
    return Promise.resolve(resp)
  } else {
    if (resp.status === 401) {
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      const { login } = storeToRefs(loginStateStore())
      login.value = false
      router.push({
        name: 'login'
      })
    }
    //@ts-ignore
    ElMessage.error(data.msg)
    return Promise.reject(resp)
  }
}, (error: AxiosError<any, any>) => {
  //@ts-ignore  
  ElMessage.error(error.response.data.msg)
  return Promise.reject(error)
})

export default axios