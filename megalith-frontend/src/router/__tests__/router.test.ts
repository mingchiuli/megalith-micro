import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { createAppRouter } from '@/router'
import { loginStateStore } from '@/stores'
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

describe('authentication routing', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('keeps an invalid cookie session on the login route', async () => {
    const GET = vi.fn().mockRejectedValue(new Error('expired session'))
    const router = createAppRouter({ server: true, api: apiWithGet(GET) })
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
    const router = createAppRouter({ server: true, api: apiWithGet(GET) })

    await router.push('/backend')

    expect(router.currentRoute.value.name).toBe('system')
    expect(loginStateStore().login).toBe(true)
    expect(loginStateStore().user).toEqual(user)
    expect(GET).toHaveBeenCalledTimes(2)
  })

  it('redirects a private route only after session restoration fails', async () => {
    const GET = vi.fn().mockRejectedValue(new Error('missing session'))
    const router = createAppRouter({ server: true, api: apiWithGet(GET) })

    await router.push('/backend')

    expect(router.currentRoute.value.name).toBe('login')
    expect(router.currentRoute.value.query.redirect).toBe('/backend')
    expect(GET).toHaveBeenCalledTimes(2)
  })

  it('does not probe authentication on a public route without memory login state', async () => {
    const GET = vi.fn()
    const router = createAppRouter({ server: true, api: apiWithGet(GET) })

    await router.push('/login')

    expect(router.currentRoute.value.name).toBe('login')
    expect(GET).not.toHaveBeenCalled()
  })
})
