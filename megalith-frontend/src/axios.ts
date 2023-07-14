import axios, { type AxiosError, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import type { Data, JWTStruct } from '@/type/entity'

axios.defaults.baseURL = 'http://127.0.0.1:8081'
axios.defaults.timeout = 10000

const convert = (token: string): JWTStruct => {
  return JSON.parse(decodeURIComponent(atob(token)))
}


axios.interceptors.request.use((config: InternalAxiosRequestConfig<any>) => {
  const token: string | null = localStorage.getItem('accessToken')
  if (token) {
    let strings: string[] = token.split(".")
    const jwt: JWTStruct = convert(strings[1])
    console.log(jwt.exp)
    const now: number = Math.floor(new Date().getTime() / 1000)
    console.log(now)
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
      // TODO
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