import { beforeEach, describe, expect, it, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { Base64 } from 'js-base64'

// 隔离对外部模块的依赖（router / axios 实例 / 业务 http 封装 / OTEL）
vi.mock('@/router', () => ({
  default: {
    push: vi.fn(),
    hasRoute: vi.fn(() => false),
    removeRoute: vi.fn()
  }
}))

vi.mock('@/http/axios', () => ({
  httpClient: { get: vi.fn(), post: vi.fn() },
  longHttpClient: { get: vi.fn(), post: vi.fn() },
  aiHttpClient: { get: vi.fn(), post: vi.fn() }
}))

vi.mock('@/http/http', () => ({
  GET: vi.fn(),
  POST: vi.fn(),
  DOWNLOAD: vi.fn(),
  UPLOAD: vi.fn()
}))

vi.mock('@/config/otel', () => ({
  createTraceParent: vi.fn(() => '00-trace-span-01')
}))

import {
  debounce,
  render,
  getJWTStruct,
  submitLogin,
  checkAccessToken,
  updateAccessToken,
  clearLoginState,
  diff,
  findMenuByPath,
  cleanJsonResponse,
  checkButtonAuth,
  getButtonType,
  getButtonTitle
} from '@/utils/tools'
import { httpClient } from '@/http/axios'
import { GET, POST } from '@/http/http'
import router from '@/router'
import {
  loginStateStore,
  authMarkStore,
  menuStore,
  tabStore,
  buttonStore
} from '@/stores/store'
import type { Menu, Button } from '@/type/entity'

const buildJWT = (payload: Record<string, unknown>): string => {
  const header = Base64.toBase64(JSON.stringify({ alg: 'HS256', typ: 'JWT' }))
  const body = Base64.toBase64(JSON.stringify(payload))
  return `${header}.${body}.signature`
}

describe('utils/tools', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    vi.clearAllMocks()
  })

  describe('debounce', () => {
    it('在间隔内多次调用只触发最后一次', () => {
      vi.useFakeTimers()
      const spy = vi.fn()
      const debounced = debounce(spy, 200)
      debounced()
      debounced()
      debounced()
      expect(spy).not.toHaveBeenCalled()
      vi.advanceTimersByTime(200)
      expect(spy).toHaveBeenCalledTimes(1)
      vi.useRealTimers()
    })

    it('保留最后一次调用的参数', () => {
      vi.useFakeTimers()
      const spy = vi.fn()
      const debounced = debounce(spy as (...args: unknown[]) => unknown, 50)
      debounced('a')
      debounced('b')
      vi.advanceTimersByTime(50)
      expect(spy).toHaveBeenCalledWith('b')
      vi.useRealTimers()
    })
  })

  describe('render', () => {
    it('将 markdown 渲染为 HTML', () => {
      const html = render('# Title\n\nhello')
      expect(html).toContain('<h1>Title</h1>')
      expect(html).toContain('<p>hello</p>')
    })

    it('支持 fenced code 高亮', () => {
      const html = render('```js\nconst a = 1\n```')
      expect(html).toContain('<pre>')
      expect(html).toContain('<code')
    })
  })

  describe('getJWTStruct', () => {
    it('从 localStorage 解析 access token 的 payload', () => {
      const payload = { userId: 42, exp: 1700000000, role: 'admin' }
      localStorage.setItem('accessToken', buildJWT(payload))
      const struct = getJWTStruct()
      expect(struct).toMatchObject(payload)
    })
  })

  describe('checkAccessToken', () => {
    it('token 即将过期时刷新并写入 localStorage', async () => {
      const soonExp = Math.floor(Date.now() / 1000) + 60
      localStorage.setItem('accessToken', buildJWT({ exp: soonExp }))
      localStorage.setItem('refreshToken', 'refresh-xyz')
      vi.mocked(httpClient.get).mockResolvedValueOnce({
        data: { data: { accessToken: 'NEW_TOKEN' } }
      } as never)

      const refreshed = await checkAccessToken()
      expect(refreshed).toBe(true)
      expect(localStorage.getItem('accessToken')).toBe('NEW_TOKEN')
    })

    it('token 仍然有效时不发起刷新', async () => {
      const farExp = Math.floor(Date.now() / 1000) + 7200
      localStorage.setItem('accessToken', buildJWT({ exp: farExp }))
      const refreshed = await checkAccessToken()
      expect(refreshed).toBe(false)
      expect(httpClient.get).not.toHaveBeenCalled()
    })
  })

  describe('submitLogin', () => {
    it('用户名或密码为空时直接返回', async () => {
      await submitLogin('', '')
      expect(POST).not.toHaveBeenCalled()
    })

    it('登录成功后写入 token 并更新 store', async () => {
      vi.mocked(POST).mockResolvedValueOnce({
        accessToken: 'AT',
        refreshToken: 'RT'
      } as never)
      vi.mocked(GET).mockResolvedValueOnce({ username: 'tom' } as never)

      await submitLogin('tom', 'pwd')

      expect(POST).toHaveBeenCalledTimes(1)
      expect(localStorage.getItem('accessToken')).toBe('AT')
      expect(localStorage.getItem('refreshToken')).toBe('RT')
      expect(localStorage.getItem('userinfo')).toBe(JSON.stringify({ username: 'tom' }))
      expect(loginStateStore().login).toBe(true)
    })
  })

  describe('updateAccessToken', () => {
    it('未过期时返回原 token 且不发起刷新请求', async () => {
      const farExp = Math.floor(Date.now() / 1000) + 7200
      localStorage.setItem('accessToken', buildJWT({ exp: farExp }))
      const token = await updateAccessToken()
      expect(token).toBe(localStorage.getItem('accessToken'))
      expect(httpClient.get).not.toHaveBeenCalled()
    })

    it('即将过期时返回刷新后的新 token', async () => {
      const soonExp = Math.floor(Date.now() / 1000) + 60
      localStorage.setItem('accessToken', buildJWT({ exp: soonExp }))
      localStorage.setItem('refreshToken', 'RT')
      vi.mocked(httpClient.get).mockResolvedValueOnce({
        data: { data: { accessToken: 'NEW' } }
      } as never)
      const token = await updateAccessToken()
      expect(token).toBe('NEW')
      expect(localStorage.getItem('accessToken')).toBe('NEW')
    })
  })

  describe('clearLoginState', () => {
    it('清空 localStorage 与登录相关 store 状态', () => {
      localStorage.setItem('accessToken', 'AT')
      localStorage.setItem('refreshToken', 'RT')
      localStorage.setItem('userinfo', '{}')

      authMarkStore().auth = true
      loginStateStore().login = true
      menuStore().menuTree = { name: 'root' } as Menu
      tabStore().editableTabs = [{ name: 'a', title: 'A' }]
      tabStore().editableTabsValue = 'a'

      vi.mocked(router.hasRoute).mockReturnValueOnce(true)

      clearLoginState()

      expect(localStorage.getItem('accessToken')).toBeNull()
      expect(localStorage.getItem('refreshToken')).toBeNull()
      expect(localStorage.getItem('userinfo')).toBeNull()
      expect(router.removeRoute).toHaveBeenCalledWith('root')
      expect(authMarkStore().auth).toBe(false)
      expect(loginStateStore().login).toBe(false)
      expect(menuStore().menuTree).toBeUndefined()
      expect(tabStore().editableTabs).toEqual([])
      expect(tabStore().editableTabsValue).toBe('')
    })
  })

  describe('diff', () => {
    it('长度不一致返回 true', () => {
      expect(diff([{ a: 1 }], [{ a: 1 }, { a: 2 }])).toBe(true)
    })

    it('完全相同返回 false', () => {
      expect(diff([{ a: 1, b: 2 }], [{ a: 1, b: 2 }])).toBe(false)
    })

    it('字段值不同返回 true', () => {
      expect(diff([{ a: 1 }], [{ a: 2 }])).toBe(true)
    })

    it('递归比较 children', () => {
      const oldArr = [{ a: 1, children: [{ a: 1 }] }]
      const newArr = [{ a: 1, children: [{ a: 2 }] }]
      expect(diff(oldArr, newArr)).toBe(true)
    })
  })

  describe('findMenuByPath', () => {
    const tree: Menu[] = [
      { url: '/a', name: 'A', children: [{ url: '/a/b', name: 'AB' } as Menu] } as Menu,
      { url: '/c', name: 'C' } as Menu
    ]

    it('命中顶层节点', () => {
      expect(findMenuByPath(tree, '/c')?.name).toBe('C')
    })

    it('命中嵌套节点', () => {
      expect(findMenuByPath(tree, '/a/b')?.name).toBe('AB')
    })

    it('未命中返回 undefined', () => {
      expect(findMenuByPath(tree, '/none')).toBeUndefined()
    })
  })

  describe('cleanJsonResponse', () => {
    it('剥离 fenced code 标记并裁剪到大括号边界', () => {
      const raw = '```json\n  {"a":1, "b":2}  \n```'
      expect(cleanJsonResponse(raw)).toBe('{"a":1, "b":2}')
    })

    it('丢弃首个 { 之前与最后一个 } 之后的噪声', () => {
      expect(cleanJsonResponse('noise{"x":1}tail')).toBe('{"x":1}')
    })
  })

  describe('button helpers', () => {
    const seed = (list: Partial<Button>[]) => {
      buttonStore().buttonList = list as Button[]
    }

    it('checkButtonAuth 按 name 命中', () => {
      seed([{ name: 'save', title: 'Save', icon: 'primary' }])
      expect(checkButtonAuth('save')).toBe(true)
      expect(checkButtonAuth('delete')).toBe(false)
    })

    it('getButtonType 返回 icon 字段', () => {
      seed([{ name: 'del', title: 'Del', icon: 'danger' }])
      expect(getButtonType('del')).toBe('danger')
    })

    it('getButtonTitle 返回 title 字段', () => {
      seed([{ name: 'edit', title: '编辑', icon: 'primary' }])
      expect(getButtonTitle('edit')).toBe('编辑')
    })
  })
})
