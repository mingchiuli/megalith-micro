import { useRouter } from 'vue-router'
import { getActivePinia } from 'pinia'
import { API_ENDPOINTS } from '@/config/apiConfig'
import { useHttp } from '@/http/http'
import { clearAuthStores } from '@/router'
import { loginStateStore } from '@/stores'
import type { LoginType, UserInfo } from '@/type/entity'

export const useAuth = () => {
  const api = useHttp()
  const router = useRouter()
  const pinia = getActivePinia()
  if (!pinia) throw new Error('Pinia is not available in the current app')

  const clearLoginState = () => clearAuthStores(router, pinia)

  const logout = async (): Promise<void> => {
    try {
      await api.POST<null>(API_ENDPOINTS.AUTH.TOKEN_LOGOUT, {})
    } finally {
      clearLoginState()
    }
  }

  const submitLogin = async (loginType: LoginType, username: string, password: string) => {
    if (!username || !password) return
    await api.POST<void>(API_ENDPOINTS.AUTH.LOGIN, {
      loginType,
      principal: username,
      credential: password
    })
    const loginState = loginStateStore(pinia)
    loginState.login = true
    loginState.user = await api.GET<UserInfo>(API_ENDPOINTS.AUTH.USER_INFO)
    const redirect = router.currentRoute.value.query.redirect
    await router.push(typeof redirect === 'string' ? redirect : '/backend')
  }

  return { clearLoginState, logout, submitLogin }
}
