import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { createAppRouter } from '@/router'
import { authMarkStore, loginStateStore, menuStore } from '@/stores'
import { RoutesEnum, RoutesStatus, type Menu, type UserInfo } from '@/type/entity'
import { API_ENDPOINTS } from '@/config/apiConfig'
import type { ApiClient } from '@/http/http'

vi.mock('@/views/LoginView.vue', () => ({ default: { template: '<div />' } }))
vi.mock('@/views/sys/SystemView.vue', () => ({ default: { template: '<div />' } }))

const apiWithGet = (GET: ReturnType<typeof vi.fn>) => ({ GET }) as unknown as ApiClient

const backendMenu: Menu = {
  id: 1,
  parentId: 0,
  title: 'Backend',
  name: 'system',
  icon: '',
  orderNum: 0,
  status: RoutesStatus.NORMAL,
  type: RoutesEnum.CATALOGUE,
  url: '/backend',
  component: 'sys/SystemView',
  children: []
}

const user: UserInfo = { id: 1, nickname: 'Chiu', avatar: '' }

const deferred = <T>() => {
  let resolve!: (value: T) => void
  let reject!: (reason?: unknown) => void
  const promise = new Promise<T>((resolvePromise, rejectPromise) => {
    resolve = resolvePromise
    reject = rejectPromise
  })
  return { promise, resolve, reject }
}

describe('authentication routing', () => {
  let pinia: ReturnType<typeof createPinia>

  beforeEach(() => {
    pinia = createPinia()
    setActivePinia(pinia)
  })

  it('keeps an invalid cookie session on the login route', async () => {
    const GET = vi.fn().mockRejectedValue(new Error('expired session'))
    const router = createAppRouter({ server: true, api: apiWithGet(GET), pinia })
    loginStateStore().login = true

    await router.push('/login')

    expect(router.currentRoute.value.name).toBe('login')
    expect(loginStateStore().login).toBe(false)
    expect(GET).toHaveBeenCalledTimes(2)
  })

  it('restores a private route before redirecting when memory login state is empty', async () => {
    const GET = vi.fn((url: string) => {
      if (url === API_ENDPOINTS.AUTH.MENU_NAV) return Promise.resolve(backendMenu)
      if (url === API_ENDPOINTS.AUTH.USER_INFO) return Promise.resolve(user)
      return Promise.reject(new Error(`Unexpected URL: ${url}`))
    })
    const router = createAppRouter({ server: true, api: apiWithGet(GET), pinia })

    await router.push('/backend')

    expect(router.currentRoute.value.name).toBe('system')
    expect(router.currentRoute.value.meta.layout).toBe('system')
    expect(loginStateStore().login).toBe(true)
    expect(loginStateStore().user).toEqual(user)
    expect(GET).toHaveBeenCalledTimes(2)
  })

  it('keeps route state isolated from another active Pinia instance', async () => {
    const routerPinia = createPinia()
    const otherPinia = createPinia()
    const GET = vi.fn((url: string) => {
      if (url === API_ENDPOINTS.AUTH.MENU_NAV) return Promise.resolve(backendMenu)
      if (url === API_ENDPOINTS.AUTH.USER_INFO) return Promise.resolve(user)
      return Promise.reject(new Error(`Unexpected URL: ${url}`))
    })
    loginStateStore(routerPinia).login = true
    setActivePinia(otherPinia)
    const router = createAppRouter({ server: true, api: apiWithGet(GET), pinia: routerPinia })

    await router.push('/backend')

    expect(loginStateStore(routerPinia).user).toEqual(user)
    expect(loginStateStore(otherPinia).user).toBeUndefined()
    expect(menuStore(routerPinia).menuTree).toEqual(backendMenu)
    expect(menuStore(otherPinia).menuTree).toBeUndefined()
  })

  it('refreshes navigation on every private route without reloading the user', async () => {
    const refreshedMenu = { ...backendMenu, title: 'Refreshed backend' }
    let menuRequests = 0
    const GET = vi.fn((url: string) => {
      if (url === API_ENDPOINTS.AUTH.MENU_NAV) {
        menuRequests += 1
        return Promise.resolve(menuRequests === 1 ? backendMenu : refreshedMenu)
      }
      if (url === API_ENDPOINTS.AUTH.USER_INFO) return Promise.resolve(user)
      return Promise.reject(new Error(`Unexpected URL: ${url}`))
    })
    const router = createAppRouter({ server: true, api: apiWithGet(GET), pinia })

    await router.push('/backend')
    await router.push('/backend?view=roles')
    await router.push('/backend?view=menus')

    expect(GET.mock.calls.filter(([url]) => url === API_ENDPOINTS.AUTH.MENU_NAV)).toHaveLength(3)
    expect(GET.mock.calls.filter(([url]) => url === API_ENDPOINTS.AUTH.USER_INFO)).toHaveLength(1)
    await vi.waitFor(() => expect(menuStore().menuTree?.title).toBe('Refreshed backend'))
  })

  it('does not wait for a navigation refresh when a cached session is available', async () => {
    const refresh = deferred<Menu>()
    const GET = vi.fn((url: string) => {
      if (url === API_ENDPOINTS.AUTH.MENU_NAV) return refresh.promise
      return Promise.reject(new Error(`Unexpected URL: ${url}`))
    })
    loginStateStore().login = true
    loginStateStore().user = user
    menuStore().menuTree = backendMenu
    authMarkStore().auth = true
    const router = createAppRouter({ server: true, api: apiWithGet(GET), pinia })

    await router.push('/backend')

    expect(router.currentRoute.value.name).toBe('system')
    expect(GET).toHaveBeenCalledTimes(1)

    refresh.resolve({ ...backendMenu, title: 'Refreshed backend' })
    await vi.waitFor(() => expect(menuStore().menuTree?.title).toBe('Refreshed backend'))
  })

  it('keeps the latest navigation response during rapid route changes', async () => {
    const firstRefresh = deferred<Menu>()
    const secondRefresh = deferred<Menu>()
    let menuRequests = 0
    const GET = vi.fn((url: string) => {
      if (url === API_ENDPOINTS.AUTH.MENU_NAV) {
        menuRequests += 1
        if (menuRequests === 1) return Promise.resolve(backendMenu)
        return menuRequests === 2 ? firstRefresh.promise : secondRefresh.promise
      }
      if (url === API_ENDPOINTS.AUTH.USER_INFO) return Promise.resolve(user)
      return Promise.reject(new Error(`Unexpected URL: ${url}`))
    })
    const router = createAppRouter({ server: true, api: apiWithGet(GET), pinia })

    await router.push('/backend')
    await router.push('/backend?view=roles')
    await router.push('/backend?view=menus')

    expect(menuRequests).toBe(3)
    secondRefresh.resolve({ ...backendMenu, title: 'Latest backend' })
    await vi.waitFor(() => expect(menuStore().menuTree?.title).toBe('Latest backend'))
    firstRefresh.resolve({ ...backendMenu, title: 'Stale backend' })
    await firstRefresh.promise
    await Promise.resolve()
    expect(menuStore().menuTree?.title).toBe('Latest backend')
  })

  it('keeps the current session when a background navigation refresh fails', async () => {
    const refresh = deferred<Menu>()
    const GET = vi.fn((url: string) => {
      if (url === API_ENDPOINTS.AUTH.MENU_NAV) return refresh.promise
      return Promise.reject(new Error(`Unexpected URL: ${url}`))
    })
    loginStateStore().login = true
    loginStateStore().user = user
    menuStore().menuTree = backendMenu
    authMarkStore().auth = true
    const router = createAppRouter({ server: true, api: apiWithGet(GET), pinia })

    await router.push('/backend')
    refresh.reject(new Error('navigation unavailable'))
    await expect(refresh.promise).rejects.toThrow('navigation unavailable')
    await Promise.resolve()

    expect(router.currentRoute.value.name).toBe('system')
    expect(loginStateStore().login).toBe(true)
    expect(menuStore().menuTree).toEqual(backendMenu)
  })

  it('redirects a private route only after session restoration fails', async () => {
    const GET = vi.fn().mockRejectedValue(new Error('missing session'))
    const router = createAppRouter({ server: true, api: apiWithGet(GET), pinia })

    await router.push('/backend')

    expect(router.currentRoute.value.name).toBe('login')
    expect(router.currentRoute.value.query.redirect).toBe('/backend')
    expect(GET).toHaveBeenCalledTimes(2)
  })

  it('does not probe authentication on a public route without memory login state', async () => {
    const GET = vi.fn()
    const router = createAppRouter({ server: true, api: apiWithGet(GET), pinia })

    await router.push('/login')

    expect(router.currentRoute.value.name).toBe('login')
    expect(GET).not.toHaveBeenCalled()
  })
})
