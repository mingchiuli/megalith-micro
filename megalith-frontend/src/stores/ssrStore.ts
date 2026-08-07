export const ssrDataStore = defineStore('ssrDataStore', () => {
  const entries = ref<Record<string, unknown>>({})

  const has = (key: string) => Object.prototype.hasOwnProperty.call(entries.value, key)
  const get = <T>(key: string): T | undefined => entries.value[key] as T | undefined
  const set = <T>(key: string, value: T) => {
    entries.value[key] = value
  }
  const remove = (key: string) => {
    delete entries.value[key]
  }

  return { entries, has, get, set, remove }
})
