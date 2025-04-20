import { longHttp, http } from '@/http/axios'
import type { Data } from '@/type/entity'
import type { AxiosProgressEvent, AxiosResponse } from 'axios'
import { type Ref } from 'vue'

const GET = async <T>(url: string): Promise<T> => {
  const response = await http.get<never, AxiosResponse<Data<T>>>(url)
  return response.data.data
}

const POST = async <T>(url: string, params: unknown): Promise<T> => {
  const response = await http.post<never, AxiosResponse<Data<T>>>(url, params)
  return response.data.data
}

const handleProgress = (
  percentage: Ref<number>,
  percentageShow: Ref<boolean>,
  progressEvent: AxiosProgressEvent
) => {
  const { loaded, total } = progressEvent
  percentage.value = Math.floor((loaded * 100) / total!)
}

const DOWNLOAD_DATA = async (
  url: string,
  percentage: Ref<number>,
  percentageShow: Ref<boolean>
): Promise<AxiosResponse<string> | null> => {
  let data: AxiosResponse<string> | null = null
  percentageShow.value = true
  percentage.value = 0
  await longHttp
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
  await longHttp
    .post(dest, formData, {
      onUploadProgress: (progressEvent) => handleProgress(percentage, percentageShow, progressEvent)
    })
    .then((resp: unknown) => {
      url = resp as string
      url = url.substring('data:'.length).replace(/\n/g, '')
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

export { GET, POST, DOWNLOAD_DATA, UPLOAD }
