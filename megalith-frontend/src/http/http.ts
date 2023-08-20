import axios from '@/http/axios'
import type { Data } from '@/type/entity'

const GET = async <T>(url: string): Promise<Data<T>> => {
  return axios.get<never, Data<T>>(url)
}

const POST = async <T>(url: string, params: FormData): Promise<Data<T>> => {
  return axios.post<never, Data<T>>(url, params)
}

export { GET, POST }
