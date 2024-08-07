import http from '@/http/axios'
import type { Data } from '@/type/entity'
import type { AxiosResponse } from 'axios'
import type { Ref } from 'vue'

const GET = async <T>(url: string): Promise<T> => {
  return Promise.resolve((await http.get<never, Data<T>>(url)).data)
}

const POST = async <T>(url: string, params: any): Promise<T> => {
  return Promise.resolve((await http.post<never, Data<T>>(url, params)).data)
}

const DOWNLOAD_DATA = async (url: string): Promise<AxiosResponse<any, any>> => {
  return await http.get<never, AxiosResponse<any, any>>(url)
}

const UPLOAD = async (dest: string, formData: FormData, percentage: Ref<number>, percentageShow: Ref<boolean>): Promise<string> => {
  percentageShow.value = true
  percentage.value = 0
  let url = ''
  http.post(dest, formData, {
    onUploadProgress: (progressEvent) => {
      const { loaded, total } = progressEvent
      percentage.value = Math.floor((loaded * 100) / total!)
    }
  }).then(res => {
    percentage.value = 100
    percentageShow.value = false
    url = res.data
  }).catch(e => {
    percentageShow.value = false
    return Promise.reject(new Error(e))
  })
  return Promise.resolve(url)
}

export { GET, POST, DOWNLOAD_DATA, UPLOAD }
