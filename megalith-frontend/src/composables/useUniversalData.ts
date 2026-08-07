import { onMounted, onServerPrefetch } from 'vue'
import { ssrDataStore } from '@/stores/ssrStore'

export const useUniversalData = <T>(
  key: string,
  loader: () => Promise<T>,
  apply: (data: T) => void
) => {
  const store = ssrDataStore()
  const cached = store.get<T>(key)
  if (store.has(key)) apply(cached as T)

  const execute = async (force = false) => {
    if (!force && store.has(key)) {
      const data = store.get<T>(key) as T
      apply(data)
      return data
    }
    const data = await loader()
    store.set(key, data)
    apply(data)
    return data
  }

  if (import.meta.env.SSR) onServerPrefetch(() => execute())
  else if (!store.has(key)) onMounted(() => void execute())

  return {
    refresh: () => execute(true),
    invalidate: () => store.remove(key)
  }
}
