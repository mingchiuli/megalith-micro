import http from '@/http/axios'
import type { Data } from '@/type/entity'

const GET = <T>(url: string): Promise<Data<T>> => {
  return http.get<never, Data<T>>(url)
}

const POST = <T>(url: string, params: FormData): Promise<Data<T>> => {
  return http.post<never, Data<T>>(url, params)
}

export { GET, POST }
