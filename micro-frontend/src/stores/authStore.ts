/**
 * Login state - tracks user login status
 */
export const loginStateStore = defineStore('loginStateStore', () => {
  const login = ref(false)
  const user = ref<UserInfo>()
  const sessionExpired = ref(false)
  const sessionExpiredRedirect = ref<string>()

  const markSessionExpired = (redirect?: string) => {
    if (sessionExpired.value) return false
    sessionExpired.value = true
    sessionExpiredRedirect.value = redirect
    return true
  }

  const clearSessionExpired = () => {
    sessionExpired.value = false
    sessionExpiredRedirect.value = undefined
  }

  return {
    login,
    user,
    sessionExpired,
    sessionExpiredRedirect,
    markSessionExpired,
    clearSessionExpired
  }
})

/**
 * Auth mark - used for route refresh handling
 */
export const authMarkStore = defineStore('authMarkStore', () => {
  const auth = ref(false)
  return { auth }
})
import type { UserInfo } from '@/type/entity'
