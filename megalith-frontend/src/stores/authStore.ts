import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * Welcome state - controls backend welcome message visibility
 */
export const welcomeStateStore = defineStore('welcomeStateStore', () => {
  const welcomeBackend = ref(true)
  return { welcomeBackend }
})

/**
 * Login state - tracks user login status
 */
export const loginStateStore = defineStore('loginStateStore', () => {
  const login = ref(false)
  return { login }
})

/**
 * Auth mark - used for route refresh handling
 */
export const authMarkStore = defineStore('authMarkStore', () => {
  const auth = ref(false)
  return { auth }
})