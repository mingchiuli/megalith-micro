import { onMounted, onServerPrefetch, readonly, ref, shallowRef, type Ref } from 'vue'
import { ssrDataStore } from '@/stores/ssrStore'

type UniversalDataOptions = {
  loading?: Ref<boolean>
}

export const useUniversalData = <T>(
  key: string,
  loader: () => Promise<T>,
  apply: (data: T) => void,
  options: UniversalDataOptions = {}
) => {
  const store = ssrDataStore()
  const hydrated = import.meta.env.SSR ? undefined : store.take<T>(key)
  const loading = options.loading ?? ref(hydrated === undefined)
  const error = shallowRef<unknown>()
  if (hydrated !== undefined) {
    apply(hydrated)
    loading.value = false
  }

  const execute = async () => {
    loading.value = true
    error.value = undefined
    try {
      const data = await loader()
      if (import.meta.env.SSR) store.set(key, data)
      apply(data)
      return data
    } catch (cause) {
      error.value = cause
      throw cause
    } finally {
      loading.value = false
    }
  }

  if (import.meta.env.SSR) onServerPrefetch(() => execute())
  else if (hydrated === undefined) onMounted(() => void execute().catch(() => undefined))

  return {
    refresh: execute,
    invalidate: () => store.remove(key),
    loading: readonly(loading),
    error: readonly(error)
  }
}
