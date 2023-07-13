import { ref, type Ref } from 'vue'
import { defineStore } from 'pinia'

export const searchStore = defineStore('searchStore', () => {
  const year: Ref<string> = ref('')
  const keywords: Ref<string> = ref('')
  return { year, keywords }
})

export const loginStateStore = defineStore('loginStateStore', () => {
  const login: Ref<boolean> = ref(false)
  return { login }
})
