import http from '@/http/axios'
import type { Data } from '@/type/entity'
import type { AxiosResponse } from 'axios'

const GET = async <T>(url: string): Promise<T> => {
  return Promise.resolve((await http.get<never, Data<T>>(url)).data)
}

const POST = async <T>(url: string, params: any): Promise<T> => {
  return Promise.resolve((await http.post<never, Data<T>>(url, params)).data)
}

const DOWNLOAD_DATA = async (url: string): Promise<AxiosResponse<any, any>> => {
  return await http.get<never, AxiosResponse<any, any>>(url)
}

export { GET, POST, DOWNLOAD_DATA }
