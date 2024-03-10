import http from '@/http/axios'
import { loginStateStore, menuStore, tabStore } from '@/stores/store'
import type { Data, JWTStruct, RefreshStruct } from '@/type/entity'
import hljs from 'highlight.js'
import { Base64 } from 'js-base64'
import MarkdownIt from 'markdown-it'

const md = new MarkdownIt({
  highlight: (str: string, lang: string) => {
    if (lang && hljs.getLanguage(lang)) {
      return hljs.highlight(str, { language: lang }).value
    }
    return ''
  }
})

export const clearLoginState = () => {
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('userinfo')
  loginStateStore().login = false
  menuStore().menuList = []
  tabStore().editableTabs = []
  tabStore().editableTabsValue = ''
}

export const render = (content: string): string => {
  return md.render(content)
}

export const debounce = (fn: Function, interval = 100) => {
  let timeout: NodeJS.Timeout
  return () => {
    clearTimeout(timeout)
    timeout = setTimeout(function (this: Function) {
      fn.apply(this)
    }, interval)
  }
}

export const checkAccessToken = async (): Promise<string> => {
  const accessToken = localStorage.getItem('accessToken')!
  const tokenArray = accessToken.split(".")
  const jwt: JWTStruct = JSON.parse(Base64.fromBase64(tokenArray[1]))
  const now = Math.floor(new Date().getTime() / 1000)
  //ten minutes
  if (jwt.exp - now < 100) {
    const refreshToken = localStorage.getItem('refreshToken')
    const data = await http.get<never, Data<RefreshStruct>>('/token/refresh', {
      headers: { Authorization: refreshToken }
    })
    const token = data.data.accessToken
    localStorage.setItem('accessToken', token)
    return token
  }
  return accessToken
}