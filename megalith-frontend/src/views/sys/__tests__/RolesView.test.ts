import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount, type VueWrapper } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ElementPlus from 'element-plus'
import * as ElIcons from '@element-plus/icons-vue'
import type { PageAdapter, RoleSys } from '@/type/entity'
import { ButtonAuth, DataPermission, Status } from '@/type/entity'

const mocks = vi.hoisted(() => ({
  GET: vi.fn(),
  POST: vi.fn(),
  DOWNLOAD: vi.fn(),
  posted: [] as Array<{ url: string; payload: unknown }>
}))

vi.mock('@/http/http', () => ({
  useHttp: () => ({ GET: mocks.GET, POST: mocks.POST, DOWNLOAD: mocks.DOWNLOAD })
}))

vi.mock('@/utils/permissions', () => ({
  checkButtonAuth: () => true,
  getButtonType: () => '',
  getButtonTitle: (name: string) =>
    ({
      [ButtonAuth.SYS_ROLE_EDIT]: 'Edit role',
      [ButtonAuth.SYS_ROLE_SAVE]: 'Save role',
      [ButtonAuth.SYS_ROLE_DATA_PERM]: 'Data permissions',
      [ButtonAuth.SYS_ROLE_DATA_SAVE]: 'Save data permissions'
    })[name] ?? name
}))

import RolesView from '@/views/sys/RolesView.vue'

const role: RoleSys = {
  id: 7,
  name: 'Editor',
  code: 'editor',
  remark: 'Edit blogs',
  created: '2026-08-19 10:00:00',
  updated: '2026-08-19 10:00:00',
  status: Status.NORMAL,
  dataPermissions: [DataPermission.BLOG_VIEW_ALL]
}

const page: PageAdapter<RoleSys> = {
  content: [role],
  totalElements: 1,
  pageSize: 5,
  pageNumber: 1
}

const findButton = (label: string) => {
  const button = Array.from(document.body.querySelectorAll('button')).find(
    (item) => item.textContent?.trim() === label
  )
  if (!button) throw new Error(`Button not found: ${label}`)
  return button
}

const mountRoles = async (): Promise<VueWrapper> => {
  const wrapper = mount(RolesView, {
    attachTo: document.body,
    global: {
      plugins: [createPinia(), ElementPlus],
      components: { ...ElIcons }
    }
  })
  await flushPromises()
  return wrapper
}

describe('RolesView data permissions', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    mocks.posted.length = 0
    mocks.GET.mockImplementation((url: string) => {
      if (url === '/sys/role/data-permission/7') {
        return Promise.resolve([DataPermission.BLOG_VIEW_ALL])
      }
      if (url === '/sys/role/info/7') {
        return Promise.resolve(role)
      }
      return Promise.resolve(page)
    })
    mocks.POST.mockImplementation((url: string, payload: unknown) => {
      mocks.posted.push({ url, payload: JSON.parse(JSON.stringify(payload)) })
      return Promise.resolve(null)
    })
    vi.stubGlobal(
      'ResizeObserver',
      class {
        observe() {}
        unobserve() {}
        disconnect() {}
      }
    )
  })

  afterEach(() => {
    document.body.innerHTML = ''
    vi.clearAllMocks()
    vi.unstubAllGlobals()
  })

  it('loads and saves permissions through the dedicated endpoints', async () => {
    const wrapper = await mountRoles()

    findButton('Data permissions').click()
    await flushPromises()

    expect(mocks.GET).toHaveBeenCalledWith('/sys/role/data-permission/7')
    expect(document.body.textContent).toContain('查看全部博客')

    findButton('Save data permissions').click()
    await flushPromises()

    expect(mocks.posted).toContainEqual({
      url: '/sys/role/data-permission/7',
      payload: [DataPermission.BLOG_VIEW_ALL]
    })
    expect(
      mocks.GET.mock.calls.filter(([url]) => String(url).startsWith('/sys/role/roles'))
    ).toHaveLength(2)
    wrapper.unmount()
  })

  it('does not send data permissions through the role save endpoint', async () => {
    const wrapper = await mountRoles()

    findButton('Edit role').click()
    await flushPromises()
    findButton('Save role').click()
    await flushPromises()

    const request = mocks.posted.find(({ url }) => url === '/sys/role/save')
    expect(request?.payload).toEqual({
      id: 7,
      name: 'Editor',
      code: 'editor',
      remark: 'Edit blogs',
      status: Status.NORMAL
    })
    expect(request?.payload).not.toHaveProperty('dataPermissions')
    wrapper.unmount()
  })
})
