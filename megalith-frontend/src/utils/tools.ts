import http from '@/http/axios'
import { DOWNLOAD_DATA, GET, POST } from '@/http/http'
import router from '@/router'
import { buttonStore, loginStateStore, menuStore, tabStore, authMarkStore } from '@/stores/store'
import { FieldType, OperateTypeCode, ActionType, type Data, type JWTStruct, type Menu, type PushActionForm, type RefreshStruct, type Tab, type Token, type UserInfo, type EditForm, Status, SensitiveType, FieldName } from '@/type/entity'
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
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('userinfo')
  if(router.hasRoute('system')) {
    router.removeRoute('system')
  }
  authMarkStore().auth = false
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

export const getJWTStruct = (): JWTStruct => {
  const accessToken = localStorage.getItem('accessToken')!
  const tokenArray = accessToken.split(".")
  return JSON.parse(Base64.fromBase64(tokenArray[1]))
}

export const checkAccessToken = async (accessToken: string): Promise<string> => {
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

//document.documentElement.scrollTo cant be used, distance is float
export const diff = (oldArr: any[], newArr: any[]) => {
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
    if (newObj.children && oldObj.children) {
      const dif = diff(newObj.children, oldObj.children)
      if (dif) {
        return true
      }
    }
  }
  return false
}

export const checkButtonAuth = (name: string) => {
  const { buttonList } = storeToRefs(buttonStore())
  return buttonList.value
    .map(item => item.name)
    .includes(name)
}

export const getButtonType = (name: string): "" | "default" | "success" | "warning" | "info" | "text" | "primary" | "danger" => {
  const { buttonList } = storeToRefs(buttonStore())
  return buttonList.value
    .filter(item => item.name == name)[0]?.icon as "" | "default" | "success" | "warning" | "info" | "text" | "primary" | "danger"
}

export const getButtonTitle = (name: string) => {
  const { buttonList } = storeToRefs(buttonStore())
  return buttonList.value
    .filter(item => item.name == name)[0]?.title
}

export const submitLogin = async (username: string, password: string) => {
  if (!username || !password) return
  const form = new FormData()
  form.append('username', username)
  form.append('password', password)
  const token = await POST<Token>('/login', form)
  localStorage.setItem('accessToken', token.accessToken)
  localStorage.setItem('refreshToken', token.refreshToken)
  loginStateStore().login = true
  const info = await GET<UserInfo>('/token/userinfo')
  localStorage.setItem('userinfo', JSON.stringify(info))
  router.push('/backend')
}

export const downloadData = async (url: string, fileName: string, percentage: Ref<number>, percentageShow: Ref<boolean>) => {
  const resp = await DOWNLOAD_DATA(url, percentage, percentageShow)
  const content = JSON.stringify(resp)
  const blob = new Blob([content], {
    type: 'application/json'
  })
  const aDom = document.createElement('a')
  aDom.download = fileName
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
      const item = findMenuByPath(menu.children, path)
      if (item) {
        return item
      }
    }
  }
}

export const dealAction = (n: string | undefined, o: string | undefined, pushActionForm: PushActionForm, fieldType: FieldType) : ActionType => {
  //全部删除
  const para = fieldType === FieldType.PARA
  if (!n) {
    pushActionForm.operateTypeCode = para ? OperateTypeCode.PARA_REMOVE : OperateTypeCode.NON_PARA_REMOVE
    return ActionType.PUSH_ACTION
  }

  //初始化新增
  if (!o) {
    pushActionForm.contentChange = n
    pushActionForm.operateTypeCode = para ? OperateTypeCode.PARA_TAIL_APPEND : OperateTypeCode.NON_PARA_TAIL_APPEND
    return ActionType.PUSH_ACTION
  }

  const nLen = n.length
  const oLen = o.length
  const minLen = Math.min(nLen, oLen)

  let indexStart = oLen
  for (let i = 0; i < minLen; i++) {
    if (n.charAt(i) !== o.charAt(i)) {
      indexStart = i
      break
    }
  }

  if (indexStart === oLen) {
    //向末尾添加
    if (oLen < nLen) {
      pushActionForm.contentChange = n.substring(indexStart)
      pushActionForm.operateTypeCode = para ? OperateTypeCode.PARA_TAIL_APPEND : OperateTypeCode.NON_PARA_TAIL_APPEND
    } else {
      //从末尾删除
      pushActionForm.indexStart = nLen
      pushActionForm.operateTypeCode = para ? OperateTypeCode.PARA_TAIL_SUBTRACT : OperateTypeCode.NON_PARA_TAIL_SUBTRACT
    }
    return ActionType.PUSH_ACTION
  }

  let oIndexEnd = -1
  let nIndexEnd = -1
  for (let i = oLen - 1, j = nLen - 1; i >= 0 && j >= 0; i--, j--) {
    if (o.charAt(i) !== n.charAt(j)) {
      oIndexEnd = i + 1
      nIndexEnd = j + 1
      break
    }
  }

  if (oIndexEnd === -1) {
    //从开头添加
    if (oLen < nLen) {
      pushActionForm.contentChange = n.substring(0, nLen - oLen)
      pushActionForm.operateTypeCode = para ? OperateTypeCode.PARA_HEAD_APPEND : OperateTypeCode.NON_PARA_HEAD_APPEND
    } else {
      //从开头删除
      pushActionForm.indexStart = oLen - nLen
      pushActionForm.operateTypeCode = para ? OperateTypeCode.PARA_HEAD_SUBTRACT : OperateTypeCode.NON_PARA_HEAD_SUBTRACT
    }
    return ActionType.PUSH_ACTION
  }

  //中间操作重复字符
  if (indexStart > oIndexEnd) {
    let contentChange
    if (nIndexEnd > oIndexEnd) {
      //增
      pushActionForm.indexStart = indexStart
      pushActionForm.indexEnd = indexStart
      contentChange = n.substring(indexStart, nIndexEnd + (indexStart - oIndexEnd))
    } else {
      //删
      contentChange = ''
      pushActionForm.indexStart = indexStart
      pushActionForm.indexEnd = indexStart + oIndexEnd - nIndexEnd
    }
    pushActionForm.contentChange = contentChange
    pushActionForm.operateTypeCode = para ? OperateTypeCode.PARA_REPLACE : OperateTypeCode.NON_PARA_REPLACE
    return ActionType.PUSH_ACTION
  }

  //中间正常插入/删除
  if (indexStart <= oIndexEnd) {
    let contentChange
    if (indexStart < nIndexEnd) {
      contentChange = n.substring(indexStart, nIndexEnd)
      pushActionForm.indexStart = indexStart
      pushActionForm.indexEnd = oIndexEnd
    } else {
      contentChange = ''
      pushActionForm.indexStart = indexStart
      pushActionForm.indexEnd = indexStart + (oIndexEnd - nIndexEnd)
    }

    pushActionForm.contentChange = contentChange
    pushActionForm.operateTypeCode = para ? OperateTypeCode.PARA_REPLACE : OperateTypeCode.NON_PARA_REPLACE
    return ActionType.PUSH_ACTION
  }
  //全不满足直接推全量数据
  return ActionType.PUSH_ALL
}

export const recheckSensitive = (pushActionForm: PushActionForm, form: EditForm) => {
  if (form.status !== Status.SENSITIVE_FILTER) {
    return
  }
  const field = pushActionForm.field
  const operateType = pushActionForm.operateTypeCode
  const len = form.sensitiveContentList.length
  const indexStart = pushActionForm.indexStart!
  /**
   * type 1
   */

  if (field === FieldName.TITLE && (operateType === OperateTypeCode.NON_PARA_REMOVE || operateType === OperateTypeCode.NON_PARA_HEAD_APPEND || operateType === OperateTypeCode.NON_PARA_HEAD_SUBTRACT)) {
    const sensitiveList = form.sensitiveContentList.filter(item => item.type !== SensitiveType.TITLE)
    if (len !== sensitiveList.length) {
      form.sensitiveContentList = sensitiveList
    }
    return
  }

  if (field === FieldName.DESCRIPTION && (operateType === OperateTypeCode.NON_PARA_REMOVE || operateType === OperateTypeCode.NON_PARA_HEAD_APPEND || operateType === OperateTypeCode.NON_PARA_HEAD_SUBTRACT)) {
    const sensitiveList = form.sensitiveContentList.filter(item => item.type !== SensitiveType.DESCRIPTION)
    if (len !== sensitiveList.length) {
      form.sensitiveContentList = sensitiveList
    }
    return
  }

  if (field === FieldName.CONTENT && (operateType === OperateTypeCode.PARA_REMOVE || operateType === OperateTypeCode.PARA_HEAD_APPEND || operateType === OperateTypeCode.PARA_HEAD_SUBTRACT)) {
    const sensitiveList = form.sensitiveContentList.filter(item => item.type !== SensitiveType.CONTENT)
    if (len !== sensitiveList.length) {
      form.sensitiveContentList = sensitiveList
    }
    return
  }

  /**
   * type 2
   */

  if (field === FieldName.TITLE && (operateType === OperateTypeCode.NON_PARA_TAIL_SUBTRACT || operateType === OperateTypeCode.NON_PARA_REPLACE)){
    const sensitiveList = form.sensitiveContentList.filter(item => item.type !== SensitiveType.TITLE || item.endIndex - 1 < indexStart)
    if (sensitiveList.length !== len) {
      form.sensitiveContentList = sensitiveList
    }
    return
  }

  if (field === FieldName.DESCRIPTION && (operateType === OperateTypeCode.NON_PARA_TAIL_SUBTRACT || operateType === OperateTypeCode.NON_PARA_REPLACE)) {
    const sensitiveList = form.sensitiveContentList.filter(item => item.type !== SensitiveType.DESCRIPTION || item.endIndex - 1 < indexStart)
    if (sensitiveList.length !== len) {
      form.sensitiveContentList = sensitiveList
    }
    return
  }

  if (field === FieldName.CONTENT && (operateType === OperateTypeCode.PARA_TAIL_SUBTRACT || operateType ===  OperateTypeCode.PARA_REPLACE)) {
    const sensitiveList = form.sensitiveContentList.filter(item => item.type !== SensitiveType.CONTENT || item.endIndex - 1 < indexStart)
    if (sensitiveList.length !== len) {
      form.sensitiveContentList = sensitiveList
    }
  }
}
