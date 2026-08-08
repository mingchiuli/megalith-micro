import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { createAppRouter } from '@/router'
import { loginStateStore } from '@/stores'
import type { ApiClient } from '@/http/http'

vi.mock('@/views/LoginView.vue', () => ({ default: { template: '<div />' } }))

const apiWithGet = (GET: ReturnType<typeof vi.fn>) => ({ GET }) as unknown as ApiClient

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
})
