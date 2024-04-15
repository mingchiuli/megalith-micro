import http from '@/http/axios'
import { GET, POST } from '@/http/http'
import router from '@/router'
import { loginStateStore, menuStore, tabStore } from '@/stores/store'
import type {  ChildrenFather, Data, JWTStruct, RefreshStruct, Token, UserInfo } from '@/type/entity'
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
  router.removeRoute('system')
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
  return (...args: any[]) => {
    clearTimeout(timeout)
    timeout = setTimeout(function (this: Function) {
      fn.apply(this, args)
    }, interval)
  }
}

export const checkAccessToken = async (): Promise<string> => {
  const accessToken = localStorage.getItem('accessToken')!
  const tokenArray = accessToken.split(".")
  const jwt: JWTStruct = JSON.parse(Base64.fromBase64(tokenArray[1]))
  const now = Math.floor(new Date().getTime() / 1000)
  //ten minutes
  if (jwt.exp - now < 600) {
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

export const diff = (oldArr: ChildrenFather[], newArr:  ChildrenFather[]) => {
  if (oldArr.length !== newArr.length) {
    return true
  }

  for (let i = 0; i < newArr.length; i++) {
    const newObj = newArr[i]
    const oldObj = oldArr[i]

    for (const key in newObj) {
      if (key === 'children') {
        continue
      }

      if (newObj[key] !== oldObj[key]) {
        return true
      }
    }
    const dif = diff(newObj.children, oldObj.children)
    if (dif) {
      return true
    }
  }
  return false
}

export const submitLogin = async (username: string, password: string) => {
  const form = new FormData()
  form.append('username', username)
  form.append('password', password)
  const token = await POST<Token>('/login', form)
  localStorage.setItem('accessToken', token.accessToken)
  localStorage.setItem('refreshToken', token.refreshToken)
  loginStateStore().login = true
  const info = await GET<UserInfo>('/token/userinfo')
  localStorage.setItem('userinfo', JSON.stringify(info))
  router.push('/blogs')
}