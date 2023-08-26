import http from '@/http/axios'
import type { Data } from '@/type/entity'

const GET = async <T>(url: string): Promise<Data<T>> => {
  return http.get<never, Data<T>>(url)
}

const POST = async <T>(url: string, params: FormData): Promise<Data<T>> => {
  return http.post<never, Data<T>>(url, params)
}

export { GET, POST }
