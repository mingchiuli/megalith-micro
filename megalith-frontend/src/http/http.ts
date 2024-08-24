import http from '@/http/axios'
import type { Data } from '@/type/entity'
import { type Ref } from 'vue'

const GET = async <T>(url: string): Promise<T> => {
  return Promise.resolve((await http.get<never, Data<T>>(url)).data)
}

const POST = async <T>(url: string, params: any): Promise<T> => {
  return Promise.resolve((await http.post<never, Data<T>>(url, params)).data)
}

const DOWNLOAD_DATA = async (url: string, percentage: Ref<number>, percentageShow: Ref<boolean>): Promise<any> => {
  let data: any
  percentageShow.value = true
  percentage.value = 0
  await http.get(url, {
    onDownloadProgress: progressEvent => {
      const { loaded, total } = progressEvent
      percentage.value = Math.floor((loaded * 100) / total!)
    },
  }).then(res => {
    data = res
    percentage.value = 100
    setTimeout(() => {
      percentageShow.value = false
    }, 500)
  }).catch(e => {
    percentageShow.value = false
    return Promise.reject(new Error(e))
  })
  
  return Promise.resolve(data)
}

const UPLOAD = async (dest: string, formData: FormData, percentage: Ref<number>, percentageShow: Ref<boolean>): Promise<string> => {
  percentageShow.value = true
  percentage.value = 0
  let url = ''
  await http.post(dest, formData, {
    headers: {
      'Accept': 'text/event-stream',
      'Content-Type': 'multipart/form-data',
    },
    responseType: 'stream',
    adapter: 'fetch',
    onUploadProgress: progressEvent => {
      const { loaded, total } = progressEvent
      console.log(`load${loaded}, total:${total}`)
      percentage.value = Math.ceil((loaded * 100) / total!)
      if (loaded === total) {
        ElNotification({
          title: '操作成功',
          message: '图片上传成功',
          type: 'success',
        })
        percentageShow.value = false
      }
    },
  }).then(async res => {
    const stream: ReadableStream = res.data
    const reader = stream.pipeThrough(new TextDecoderStream()).getReader()
    url = (await reader.read()).value!
  }).catch(e => {
    percentageShow.value = false
    return Promise.reject(new Error(e))
  })
  return Promise.resolve(url)
}

export { GET, POST, DOWNLOAD_DATA, UPLOAD }
