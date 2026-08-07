import { useRouter } from 'vue-router'
import { API_ENDPOINTS } from '@/config/apiConfig'
import { useHttp } from '@/http/http'
import { clearAuthStores } from '@/router'
import { loginStateStore } from '@/stores'
import type { LoginType, UserInfo } from '@/type/entity'

export const useAuth = () => {
  const api = useHttp()
  const router = useRouter()

  const clearLoginState = () => clearAuthStores(router)

  const logout = async (): Promise<void> => {
    try {
      await api.POST<null>(API_ENDPOINTS.AUTH.TOKEN_LOGOUT, {})
    } finally {
      clearLoginState()
    }
  }

  const submitLogin = async (loginType: LoginType, username: string, password: string) => {
    if (!username || !password) return
    await api.POST<unknown>(API_ENDPOINTS.AUTH.LOGIN, {
      loginType,
      principal: username,
      credential: password
    })
    loginStateStore().login = true
    loginStateStore().user = await api.GET<UserInfo>(API_ENDPOINTS.AUTH.USER_INFO)
    const redirect = router.currentRoute.value.query.redirect
    await router.push(typeof redirect === 'string' ? redirect : '/backend')
  }

  return { clearLoginState, logout, submitLogin }
}
