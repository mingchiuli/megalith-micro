import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import {
  loginStateStore,
  themeStore,
  tabStore,
  welcomeStateStore,
  authMarkStore,
  menuStore,
  buttonStore,
  blogsStore,
  pageStore
} from '@/stores/store'
import type { Menu, Button } from '@/type/entity'

describe('stores/store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    document.documentElement.classList.remove('dark')
  })

  describe('loginStateStore', () => {
    it('初始 login 状态为 false', () => {
      expect(loginStateStore().login).toBe(false)
    })

    it('可被 action / 直接赋值修改', () => {
      const store = loginStateStore()
      store.login = true
      expect(loginStateStore().login).toBe(true)
    })
  })

  describe('themeStore', () => {
    beforeEach(() => {
      // 默认提供 light 偏好
      vi.stubGlobal('matchMedia', vi.fn().mockReturnValue({
        matches: false,
        media: '(prefers-color-scheme: dark)',
        addEventListener: vi.fn(),
        removeEventListener: vi.fn()
      }))
    })

    it('依据系统偏好初始化为 light', () => {
      const store = themeStore()
      expect(store.isDark).toBe(false)
    })

    it('系统偏好为 dark 时初始化为 dark', () => {
      vi.stubGlobal('matchMedia', vi.fn().mockReturnValue({
        matches: true,
        media: '(prefers-color-scheme: dark)',
        addEventListener: vi.fn(),
        removeEventListener: vi.fn()
      }))
      // 重新创建 pinia 触发 store setup
      setActivePinia(createPinia())
      const store = themeStore()
      expect(store.isDark).toBe(true)
    })

    it('toggleTheme 翻转状态并同步到 <html> class', () => {
      const store = themeStore()
      expect(document.documentElement.classList.contains('dark')).toBe(false)

      store.toggleTheme()
      expect(store.isDark).toBe(true)
      expect(document.documentElement.classList.contains('dark')).toBe(true)

      store.toggleTheme()
      expect(store.isDark).toBe(false)
      expect(document.documentElement.classList.contains('dark')).toBe(false)
    })

    it('initTheme 根据当前 isDark 写入 class', () => {
      const store = themeStore()
      store.isDark = true
      store.initTheme()
      expect(document.documentElement.classList.contains('dark')).toBe(true)
    })
  })

  describe('tabStore', () => {
    it('addTab 新增唯一 tab 并切换 active 值', () => {
      const store = tabStore()
      store.addTab({ name: 'home', title: 'Home' })
      store.addTab({ name: 'home', title: 'Home' })
      store.addTab({ name: 'about', title: 'About' })
      expect(store.editableTabs).toHaveLength(2)
      expect(store.editableTabsValue).toBe('about')
    })
  })

  describe('welcomeStateStore', () => {
    it('默认 welcomeBackend 为 true', () => {
      expect(welcomeStateStore().welcomeBackend).toBe(true)
    })
  })

  describe('authMarkStore', () => {
    it('默认 auth 为 false 且可被修改', () => {
      const store = authMarkStore()
      expect(store.auth).toBe(false)
      store.auth = true
      expect(authMarkStore().auth).toBe(true)
    })
  })

  describe('menuStore', () => {
    it('默认 menuTree 为 undefined，可写入与重置', () => {
      const store = menuStore()
      expect(store.menuTree).toBeUndefined()
      store.menuTree = { name: 'root' } as Menu
      expect(menuStore().menuTree?.name).toBe('root')
      store.menuTree = undefined
      expect(menuStore().menuTree).toBeUndefined()
    })
  })

  describe('buttonStore', () => {
    it('默认 buttonList 为空数组', () => {
      expect(buttonStore().buttonList).toEqual([])
    })

    it('可批量赋值并被读取', () => {
      const list = [{ name: 'save', title: 'Save', icon: 'primary' }] as Button[]
      buttonStore().buttonList = list
      expect(buttonStore().buttonList).toHaveLength(1)
      expect(buttonStore().buttonList[0]?.name).toBe('save')
    })
  })

  describe('blogsStore', () => {
    it('页码与关键词的初始值', () => {
      const store = blogsStore()
      expect(store.pageNum).toBe(1)
      expect(store.searchPageNum).toBe(1)
      expect(store.keywords).toBe('')
    })

    it('支持修改 keywords / pageNum', () => {
      const store = blogsStore()
      store.keywords = 'vue'
      store.pageNum = 3
      expect(blogsStore().keywords).toBe('vue')
      expect(blogsStore().pageNum).toBe(3)
    })
  })

  describe('pageStore', () => {
    it('默认 front 为 true', () => {
      expect(pageStore().front).toBe(true)
    })
  })
})
