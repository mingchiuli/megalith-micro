import { longHttpClient, httpClient } from '@/http/axios'
import type { Data } from '@/type/entity'
import type { AxiosProgressEvent, AxiosResponse } from 'axios'
import { type Ref } from 'vue'

const GET = async <T>(url: string): Promise<T> => {
  const response = await httpClient.get<never, AxiosResponse<Data<T>>>(url)
  return response.data.data
}

const POST = async <T>(url: string, params: unknown): Promise<T> => {
  const response = await httpClient.post<never, AxiosResponse<Data<T>>>(url, params)
  return response.data.data
}

const handleProgress = (
  percentage: Ref<number>,
  _: Ref<boolean>,
  progressEvent: AxiosProgressEvent
) => {
  const { loaded, total } = progressEvent
  percentage.value = Math.floor((loaded * 100) / total!)
}

const DOWNLOAD = async (
  url: string,
  percentage: Ref<number>,
  percentageShow: Ref<boolean>
): Promise<AxiosResponse<string> | null> => {
  let data: AxiosResponse<string> | null = null
  percentageShow.value = true
  percentage.value = 0
  await longHttpClient
    .get(url, {
      onDownloadProgress: (progressEvent) =>
        handleProgress(percentage, percentageShow, progressEvent)
    })
    .then((res) => {
      data = res
    })
    .catch((e) => {
      return Promise.reject(new Error(e))
    })
    .finally(() => {
      setTimeout(() => {
        percentageShow.value = false
      }, 500)
    })

  return Promise.resolve(data)
}

const UPLOAD = async (
  dest: string,
  formData: FormData,
  percentage: Ref<number>,
  percentageShow: Ref<boolean>
): Promise<string> => {
  percentageShow.value = true
  percentage.value = 0
  let url = ''
  await longHttpClient
    .post(dest, formData, {
      onUploadProgress: (progressEvent) => handleProgress(percentage, percentageShow, progressEvent)
    })
    .then((resp: AxiosResponse<Data<string>>) => {
      url = resp.data.data
    })
    .catch((e) => {
      return Promise.reject(new Error(e))
    })
    .finally(() => {
      setTimeout(() => {
        percentageShow.value = false
      }, 500)
    })
  return Promise.resolve(url)
}

export { GET, POST, DOWNLOAD, UPLOAD }
