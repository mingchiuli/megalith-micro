import { longHttp, http } from '@/http/axios'
import type { Data } from '@/type/entity'
import { clearLoginState } from '@/utils/tools'
import type { AxiosError, AxiosProgressEvent, AxiosResponse } from 'axios'
import { type Ref } from 'vue'
import router from '@/router'

const GET = async <T>(url: string): Promise<T> => {
  try {
    const response = await http.get<T, AxiosResponse<Data<T>>>(url)
    
    if (response.status === 200) {
      return response.data.data
    } else {
      ElNotification.error({
        title: 'Request Error',
        message: response.data.msg,
        showClose: true
      })
      throw new Error(response.data.msg)
    }
  } catch (error) {
    handleError(error as AxiosError<Data<T>>)
    throw error
  }
}

const POST = async <T>(url: string, params: unknown): Promise<T> => {
  try {
    const response = await http.post<T, AxiosResponse<Data<T>>>(url, params)
    
    if (response.status === 200) {
      return response.data.data
    } else {
      ElNotification.error({
        title: 'Request Error',
        message: response.data.msg,
        showClose: true
      })
      throw new Error(response.data.msg)
    }
  } catch (error) {
    handleError(error as AxiosError<Data<T>>)
    throw error
  }
}

const handleError = (error: AxiosError<Data<unknown>>) => {
  ElNotification.error({
    title: error.code,
    message: error.response?.data.msg ?? error.message,
    showClose: true
  })
  
  if (error.response?.status === 403) {
    clearLoginState()
    router.push({
      name: 'login'
    })
  }
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
): Promise<unknown> => {
  let data: unknown
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
