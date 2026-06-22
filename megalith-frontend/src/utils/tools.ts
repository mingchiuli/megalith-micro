import { httpClient } from '@/http/axios'
import { DOWNLOAD, GET, POST } from '@/http/http'
import router from '@/router'
import { buttonStore, loginStateStore, menuStore, tabStore, authMarkStore } from '@/stores'
import {
  type Data,
  type JWTStruct,
  type Menu,
  type RefreshStruct,
  type Tab,
  type Token,
  type UserInfo
} from '@/type/entity'
import type { AxiosResponse } from 'axios'
import hljs from 'highlight.js'
import { Base64 } from 'js-base64'
import MarkdownIt from 'markdown-it'
import type { Ref } from 'vue'
import { API_ENDPOINTS } from '@/config/apiConfig'
import { storage } from '@/utils/storage'
import { sanitizeHtml } from '@/utils/sanitize'

const md = new MarkdownIt({
  highlight: (str: string, lang: string) => {
    if (lang && hljs.getLanguage(lang)) {
      return hljs.highlight(str, { language: lang }).value
    }
    return ''
  }
})

export const clearLoginState = () => {
  storage.clearAuth()
  const rootName = menuStore().menuTree?.name
  if (rootName && router.hasRoute(rootName)) {
    router.removeRoute(rootName)
  }
  authMarkStore().auth = false
  loginStateStore().login = false
  menuStore().menuTree = undefined
  tabStore().editableTabs = []
  tabStore().editableTabsValue = ''
}

export const render = (content: string): string => {
  return sanitizeHtml(md.render(content))
}

export const debounce = <T extends (...args: unknown[]) => unknown>(
  fn: T,
  interval = 100
): ((...args: Parameters<T>) => void) => {
  let timeout: NodeJS.Timeout | undefined

  return function (this: ThisParameterType<T>, ...args: Parameters<T>): void {
    clearTimeout(timeout)
    timeout = setTimeout(() => {
      fn.apply(this, args)
    }, interval)
  }
}

export const getJWTStruct = (): JWTStruct => {
  const accessToken = storage.getAccessToken()
  if (!accessToken) {
    throw new Error('No access token found')
  }
  const tokenArray = accessToken.split('.')
  return JSON.parse(Base64.fromBase64(tokenArray[1]!))
}

// Token refresh lock mechanism to prevent concurrent refreshes
let refreshPromise: Promise<string> | null = null
let lastRefreshTime = 0
const REFRESH_COoldown = 5 // seconds

// Reset function for testing purposes
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

  // If token still valid for more than 10 minutes, return it
  if (jwt.exp - now > 600) {
    return accessToken
  }

  // Prevent rapid repeated refreshes
  if (now - lastRefreshTime < REFRESH_COoldown) {
    return accessToken
  }

  // Check if refresh is already in progress - wait for it
  if (refreshPromise) {
    return refreshPromise
  }

  // Create and store the refresh promise
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
      // On error, return current token
      return accessToken
    } finally {
      // Clear lock after short delay to prevent deadlock
      setTimeout(() => {
        refreshPromise = null
      }, 1000)
    }
  })()

  return refreshPromise
}

// Re-export for backward compatibility - now just calls updateAccessToken
export const checkAccessToken = async (): Promise<boolean> => {
  const oldToken = storage.getAccessToken()
  const newToken = await updateAccessToken()
  return oldToken !== newToken
}

//document.documentElement.scrollTo cant be used, distance is float
export const diff = <T extends { [key: string]: unknown }>(oldArr: T[], newArr: T[]): boolean => {
  if (oldArr.length !== newArr.length) {
    return true
  }

  for (let i = 0; i < newArr.length; i++) {
    const newObj = newArr[i]!
    const oldObj = oldArr[i]!

    const newObjKeys = Object.keys(newObj)
    for (const key of newObjKeys) {
      if (key === 'children') {
        continue
      }

      if (newObj[key] !== oldObj[key]) {
        return true
      }
    }

    const newChildren = newObj['children'] as T[]
    const oldChildren = oldObj['children'] as T[]

    if (
      'children' in newObj &&
      'children' in oldObj &&
      Array.isArray(newChildren) &&
      Array.isArray(oldChildren)
    ) {
      const dif = diff(newChildren, oldChildren)
      if (dif) {
        return true
      }
    } else if ('children' in newObj !== 'children' in oldObj || newChildren !== oldChildren) {
      return true
    }
  }
  return false
}

export const checkButtonAuth = (name: string) => {
  const { buttonList } = storeToRefs(buttonStore())
  return buttonList.value.map((item) => item.name).includes(name)
}

export const getButtonType = (
  name: string
): '' | 'default' | 'success' | 'warning' | 'info' | 'text' | 'primary' | 'danger' => {
  const { buttonList } = storeToRefs(buttonStore())
  return buttonList.value.find((item) => item.name == name)?.icon as
    | ''
    | 'default'
    | 'success'
    | 'warning'
    | 'info'
    | 'text'
    | 'primary'
    | 'danger'
}

export const getButtonTitle = (name: string) => {
  const { buttonList } = storeToRefs(buttonStore())
  return buttonList.value.find((item) => item.name == name)?.title
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
  await router.push('/backend')//wait page jump
}

export const downloadSQLData = async (
  url: string,
  fileName: string,
  percentage: Ref<number>,
  percentageShow: Ref<boolean>
) => {
  const resp = await DOWNLOAD(url, percentage, percentageShow)
  const blob = new Blob([String(resp?.data)], {
    type: 'text/plain'
  })
  const aDom = document.createElement('a')
  aDom.download = fileName + '.sql'
  aDom.style.display = 'none'
  aDom.href = URL.createObjectURL(blob)
  document.body.appendChild(aDom)
  aDom.click()
  document.body.removeChild(aDom)
}

export const findMenuByPath = (menus: Menu[], path: string): Menu | Tab | undefined => {
  for (const menu of menus) {
    if (menu.url === path) {
      return menu
    }
    if (menu.children) {
      const item = findMenuByPath(menu.children as Menu[], path)
      if (item) {
        return item
      }
    }
  }
}

export const cleanJsonResponse = (response: string): string => {
  // 移除可能的代码块标记
  let cleaned = response.replace(/```json\n?/g, '').replace(/```\n?/g, '')

  // 移除开头和结尾的空白字符
  cleaned = cleaned.trim()

  // 如果第一个字符不是 {，尝试找到第一个 { 开始的位置
  if (!cleaned.startsWith('{')) {
    const start = cleaned.indexOf('{')
    if (start !== -1) {
      cleaned = cleaned.slice(start)
    }
  }

  // 如果最后一个字符不是 }，尝试找到最后一个 } 结束的位置
  if (!cleaned.endsWith('}')) {
    const end = cleaned.lastIndexOf('}')
    if (end !== -1) {
      cleaned = cleaned.slice(0, end + 1)
    }
  }

  return cleaned
}
