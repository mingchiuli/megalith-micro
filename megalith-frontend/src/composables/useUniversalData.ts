import { onMounted, onServerPrefetch } from 'vue'
import { ssrDataStore } from '@/stores/ssrStore'

export const useUniversalData = <T>(
  key: string,
  loader: () => Promise<T>,
  apply: (data: T) => void
) => {
  const store = ssrDataStore()
  const hydrated = import.meta.env.SSR ? undefined : store.take<T>(key)
  if (hydrated !== undefined) apply(hydrated)

  const execute = async () => {
    const data = await loader()
    if (import.meta.env.SSR) store.set(key, data)
    apply(data)
    return data
  }

  if (import.meta.env.SSR) onServerPrefetch(() => execute())
  else if (hydrated === undefined) onMounted(() => void execute())

  return {
    refresh: execute,
    invalidate: () => store.remove(key)
  }
}
