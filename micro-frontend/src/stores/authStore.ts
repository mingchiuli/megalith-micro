/**
 * Login state - tracks user login status
 */
export const loginStateStore = defineStore('loginStateStore', () => {
  const login = ref(false)
  const user = ref<UserInfo>()
  return { login, user }
})

/**
 * Auth mark - used for route refresh handling
 */
export const authMarkStore = defineStore('authMarkStore', () => {
  const auth = ref(false)
  return { auth }
})
import type { UserInfo } from '@/type/entity'
