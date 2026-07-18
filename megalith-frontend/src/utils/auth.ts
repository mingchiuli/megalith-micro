import { httpClient } from '@/http/axios'
import { GET, POST } from '@/http/http'
import router from '@/router'
import { authMarkStore, buttonStore, loginStateStore, menuStore, tabStore } from '@/stores'
import type { Data, JWTStruct, RefreshStruct, Token, UserInfo } from '@/type/entity'
import type { AxiosResponse } from 'axios'
import { Base64 } from 'js-base64'
import { API_ENDPOINTS } from '@/config/apiConfig'
import { storage } from '@/utils/storage'

export const clearLoginState = () => {
  storage.clearAuth()
  const rootName = menuStore().menuTree?.name
  if (rootName && router.hasRoute(rootName)) {
    router.removeRoute(rootName)
  }
  authMarkStore().auth = false
  loginStateStore().login = false
  menuStore().menuTree = undefined
  buttonStore().buttonList = []
  tabStore().editableTabs = []
  tabStore().editableTabsValue = ''
}

export const getJWTStruct = (): JWTStruct => {
  const accessToken = storage.getAccessToken()
  if (!accessToken) {
    throw new Error('No access token found')
  }
  const tokenArray = accessToken.split('.')
  return JSON.parse(Base64.fromBase64(tokenArray[1]!))
}

let refreshPromise: Promise<string> | null = null
let lastRefreshTime = 0
const REFRESH_COOLDOWN_SECONDS = 5

export const resetTokenRefreshState = () => {
  refreshPromise = null
  lastRefreshTime = 0
}

export const updateAccessToken = async (): Promise<string> => {
  const accessToken = storage.getAccessToken()
  if (!accessToken) {
    return ''
  }

  const tokenArray = accessToken.split('.')
  if (tokenArray.length < 2) {
    return accessToken
  }

  const jwt: JWTStruct = JSON.parse(Base64.fromBase64(tokenArray[1]!))
  const now = Math.floor(Date.now() / 1000)

  if (jwt.exp - now > 600 || now - lastRefreshTime < REFRESH_COOLDOWN_SECONDS) {
    return accessToken
  }

  if (refreshPromise) {
    return refreshPromise
  }

  refreshPromise = (async () => {
    try {
      const refreshToken = storage.getRefreshToken()
      if (!refreshToken) {
        return accessToken
      }

      const data = await httpClient.get<never, AxiosResponse<Data<RefreshStruct>>>(
        API_ENDPOINTS.AUTH.TOKEN_REFRESH,
        {
          headers: { Authorization: refreshToken }
        }
      )
      const token = data.data.data.accessToken
      storage.setAccessToken(token)
      lastRefreshTime = Math.floor(Date.now() / 1000)
      return token
    } catch {
      return accessToken
    } finally {
      setTimeout(() => {
        refreshPromise = null
      }, 1000)
    }
  })()

  return refreshPromise
}

export const checkAccessToken = async (): Promise<boolean> => {
  const oldToken = storage.getAccessToken()
  const newToken = await updateAccessToken()
  return oldToken !== newToken
}

export const submitLogin = async (username: string, password: string) => {
  if (!username || !password) return
  const form = new FormData()
  form.append('username', username)
  form.append('password', password)
  const token = await POST<Token>(API_ENDPOINTS.AUTH.LOGIN, form)
  storage.setAccessToken(token.accessToken)
  storage.setRefreshToken(token.refreshToken)
  loginStateStore().login = true
  const info = await GET<UserInfo>(API_ENDPOINTS.AUTH.USER_INFO)
  storage.setUserInfo(info)
  await router.push('/backend')
}
