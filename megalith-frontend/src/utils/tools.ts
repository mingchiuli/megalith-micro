import { http } from '@/http/axios'
import { DOWNLOAD_DATA, GET, POST } from '@/http/http'
import router from '@/router'
import { buttonStore, loginStateStore, menuStore, tabStore, authMarkStore } from '@/stores/store'
import {
  type Data,
  type JWTStruct,
  type Menu,
  type RefreshStruct,
  type Tab,
  type Token,
  type UserInfo
} from '@/type/entity'
import hljs from 'highlight.js'
import { Base64 } from 'js-base64'
import MarkdownIt from 'markdown-it'
import { storeToRefs } from 'pinia'
import type { Ref } from 'vue'

const md = new MarkdownIt({
  highlight: (str: string, lang: string) => {
    if (lang && hljs.getLanguage(lang)) {
      return hljs.highlight(str, { language: lang }).value
    }
    return ''
  }
})

export const clearLoginState = () => {
  removeLocalStorageItems(['accessToken', 'refreshToken', 'userinfo'])
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

const removeLocalStorageItems = (items: string[]) => {
  items.forEach((item) => localStorage.removeItem(item))
}

const getLocalStorageItem = (key: string): string | null => {
  return localStorage.getItem(key)
}

export const render = (content: string): string => {
  return md.render(content)
}

export const debounce = <T extends (...args: unknown[]) => unknown>(
  fn: T,
  interval = 100
): ((...args: Parameters<T>) => void) => {
  let timeout: NodeJS.Timeout | undefined
  
  return function(this: ThisParameterType<T>, ...args: Parameters<T>): void {
    clearTimeout(timeout)
    timeout = setTimeout(() => {
      fn.apply(this, args)
    }, interval)
  }
}

export const getJWTStruct = (): JWTStruct => {
  const accessToken = localStorage.getItem('accessToken')!
  const tokenArray = accessToken.split('.')
  return JSON.parse(Base64.fromBase64(tokenArray[1]))
}

export const checkAccessToken = async (accessToken: string): Promise<string> => {
  const tokenArray = accessToken.split('.')
  const jwt: JWTStruct = JSON.parse(Base64.fromBase64(tokenArray[1]))
  const now = Math.floor(new Date().getTime() / 1000)
  //ten minutes
  if (jwt.exp - now < 600) {
    const refreshToken = getLocalStorageItem('refreshToken')
    const data = await http.get<never, Data<RefreshStruct>>('/token/refresh', {
      headers: { Authorization: refreshToken }
    })
    const token = data.data.accessToken
    setLocalStorageItem('accessToken', token)
    return token
  }
  return accessToken
}

//document.documentElement.scrollTo cant be used, distance is float
export const diff = <T extends { [key: string]: unknown }>(oldArr: T[], newArr: T[]): boolean => {
  if (oldArr.length !== newArr.length) {
    return true
  }

  for (let i = 0; i < newArr.length; i++) {
    const newObj = newArr[i]
    const oldObj = oldArr[i]

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
    } else if (
      ('children' in newObj) !== ('children' in oldObj) ||
      newChildren !== oldChildren
    ) {
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
  const token = await POST<Token>('/login', form)
  setLocalStorageItem('accessToken', token.accessToken)
  setLocalStorageItem('refreshToken', token.refreshToken)
  loginStateStore().login = true
  const info = await GET<UserInfo>('/token/userinfo')
  setLocalStorageItem('userinfo', JSON.stringify(info))
  router.push('/backend')
}

const setLocalStorageItem = (key: string, value: string) => {
  localStorage.setItem(key, value)
}

export const downloadSQLData = async (
  url: string,
  fileName: string,
  percentage: Ref<number>,
  percentageShow: Ref<boolean>
) => {
  const resp = await DOWNLOAD_DATA(url, percentage, percentageShow)
  const blob = new Blob([String(resp)], {
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
