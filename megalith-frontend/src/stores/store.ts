import { ref } from 'vue'
import { defineStore } from 'pinia'

export const loginStateStore = defineStore('loginStateStore', () => {
  const login = ref(false)
  return { login }
})
