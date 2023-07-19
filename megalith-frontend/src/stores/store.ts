import { ref, type Ref } from 'vue'
import { defineStore } from 'pinia'

export const loginStateStore = defineStore('loginStateStore', () => {
  const login: Ref<boolean> = ref(false)
  return { login }
})
