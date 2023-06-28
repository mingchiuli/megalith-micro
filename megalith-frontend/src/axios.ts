import axios, { AxiosError, type AxiosResponse, type InternalAxiosRequestConfig } from "axios";
import { ElMessage }  from 'element-plus'
import type { Data } from "./type/entity";

axios.defaults.baseURL = 'http://127.0.0.1:8081'
axios.defaults.timeout = 10000

axios.interceptors.request.use((config : InternalAxiosRequestConfig<any>) => {
  return config
})

axios.interceptors.response.use((resp : AxiosResponse<Data<any>, any>) => {
  const data : Data<any> = resp.data
  if (data.status === 200) {
    return Promise.resolve(resp)
  } else {
    if (data.status === 401) {
      // TODO
    }
    ElMessage.error(data.msg)
    return Promise.reject(resp)
  }
}, (error : AxiosError<any, any>) => {
  ElMessage.error(error.message)
  return Promise.reject(error)
})

export default axios