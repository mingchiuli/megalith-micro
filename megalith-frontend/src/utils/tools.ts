import http from '@/http/axios'
import { loginStateStore, menuStore, tabStore } from '@/stores/store'
import type { CatalogueLabel, Data, JWTStruct, Menu, RefreshStruct } from '@/type/entity'
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

export const diffMenus = (menusOld: Menu[], menusNew: Menu[]): boolean => {
  if (menusOld.length !== menusNew.length) {
    return true
  }

  for (let i = 0; i < menusNew.length; i++) {
    const idNew = menusNew[i].menuId
    const idOld = menusOld[i].menuId
    const componentNew = menusNew[i].component
    const componentOld = menusOld[i].component
    const urlNew = menusNew[i].url
    const urlOld = menusOld[i].url
    const iconNew = menusNew[i].icon
    const iconOld = menusOld[i].icon
    const orderNumNew = menusNew[i].orderNum
    const orderNumOld = menusOld[i].orderNum
    const nameNew = menusNew[i].name
    const nameOld = menusOld[i].name
    const parentIdNew = menusNew[i].parentId
    const parentIdOld = menusOld[i].parentId
    const statusNew = menusNew[i].status
    const statusOld = menusOld[i].status
    const titleNew = menusNew[i].title
    const titleOld = menusOld[i].title
    const typeNew = menusNew[i].type
    const typeOld = menusOld[i].type

    if (idNew !== idOld || componentNew !== componentOld || urlNew !== urlOld || iconNew !== iconOld || orderNumNew !== orderNumOld || nameNew !== nameOld || parentIdNew !== parentIdOld || statusNew !== statusOld || titleNew !== titleOld || typeNew !== typeOld) {
      return true
    }
    const diff = diffMenus(menusNew[i].children, menusOld[i].children)
    if (diff) {
      return true
    }
  }
  return false
}

export const diffCatalogue = (cataloguesOld: CatalogueLabel[], cataloguesNew: CatalogueLabel[]): boolean => {
  if (cataloguesOld.length !== cataloguesNew.length) {
    return true
  }

  for (let i = 0; i < cataloguesNew.length; i++) {
    const newDist = cataloguesNew[i].dist
    const oldDist = cataloguesOld[i].dist
    if (newDist !== oldDist) {
      return true
    }
    const diff = diffCatalogue(cataloguesNew[i].children, cataloguesOld[i].children)
    if (diff) {
      return true
    }
  }
  return false
}