import type { AxiosInstance, AxiosProgressEvent, AxiosRequestConfig, AxiosResponse } from 'axios'
import { inject, type InjectionKey, type Ref } from 'vue'
import type { Data } from '@/type/entity'
import type { HttpClients } from '@/http/axios'

export type ApiClient = ReturnType<typeof createApiClient>

export const API_CLIENT_KEY: InjectionKey<ApiClient> = Symbol('api-client')

export const createApiClient = ({ httpClient, longHttpClient, aiHttpClient }: HttpClients) => {
  const GET = async <T>(url: string, config?: AxiosRequestConfig): Promise<T> => {
    const response = await httpClient.get<never, AxiosResponse<Data<T>>>(url, config)
    return response.data.data
  }

  const POST = async <T>(url: string, params: unknown): Promise<T> => {
    const response = await httpClient.post<never, AxiosResponse<Data<T>>>(url, params)
    return response.data.data
  }

  const DELETE = async <T>(url: string, params: unknown): Promise<T> => {
    const response = await httpClient.delete<never, AxiosResponse<Data<T>>>(url, { data: params })
    return response.data.data
  }

  const handleProgress = (percentage: Ref<number>, progressEvent: AxiosProgressEvent) => {
    const { loaded, total } = progressEvent
    percentage.value = total ? Math.min(100, Math.floor((loaded * 100) / total)) : 0
  }

  const DOWNLOAD = async (
    url: string,
    percentage: Ref<number>,
    percentageShow: Ref<boolean>
  ): Promise<AxiosResponse<string>> => {
    percentageShow.value = true
    percentage.value = 0
    try {
      return await longHttpClient.get(url, {
        onDownloadProgress: (event) => handleProgress(percentage, event)
      })
    } finally {
      globalThis.setTimeout(() => {
        percentageShow.value = false
      }, 500)
    }
  }

  const UPLOAD = async (
    dest: string,
    formData: FormData,
    percentage: Ref<number>,
    percentageShow: Ref<boolean>
  ): Promise<string> => {
    percentageShow.value = true
    percentage.value = 0
    try {
      const response = await longHttpClient.post<never, AxiosResponse<Data<string>>>(
        dest,
        formData,
        {
          onUploadProgress: (event) => handleProgress(percentage, event)
        }
      )
      return response.data.data
    } finally {
      globalThis.setTimeout(() => {
        percentageShow.value = false
      }, 500)
    }
  }

  return { GET, POST, DELETE, DOWNLOAD, UPLOAD, httpClient, longHttpClient, aiHttpClient }
}

export const useHttp = (): ApiClient => {
  const api = inject(API_CLIENT_KEY)
  if (!api) throw new Error('API client is not available in the current app')
  return api
}

export type { AxiosInstance }
