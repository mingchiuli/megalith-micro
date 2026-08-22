import type { Ref } from 'vue'

export const useLatestRequest = (loading: Ref<boolean>) => {
  let latestRequest = 0

  const runLatest = async <T>(loader: () => Promise<T>, apply: (data: T) => void): Promise<T> => {
    const request = ++latestRequest
    loading.value = true
    try {
      const data = await loader()
      if (request === latestRequest) apply(data)
      return data
    } finally {
      if (request === latestRequest) loading.value = false
    }
  }

  return { runLatest }
}
