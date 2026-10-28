import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount, type VueWrapper } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ElementPlus from 'element-plus'
import * as ElIcons from '@element-plus/icons-vue'
import type { PageAdapter, RoleSys } from '@/type/entity'
import { ButtonAuth, DataPermission, Status } from '@/type/entity'
import type { RoleMenuForm } from '@/composables/admin/useRoleEditor'

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
      [ButtonAuth.SYS_ROLE_MENU_PERM]: 'Menu permissions',
      [ButtonAuth.SYS_MENU_AUTHORITY_SAVE]: 'Save menu permissions',
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

let menuTree: RoleMenuForm[]

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

const menuCheckbox = (title: string) => {
  const label = Array.from(
    document.body.querySelectorAll<HTMLElement>('.el-tree-node__label')
  ).find((item) => item.textContent?.trim() === title)
  if (!label) throw new Error(`Menu node not found: ${title}`)
  const checkbox = label
    .closest('.el-tree-node__content')
    ?.querySelector<HTMLElement>('.el-checkbox')
  if (!checkbox) throw new Error(`Menu checkbox not found: ${title}`)
  return checkbox
}

const menuCheckboxState = (title: string) => {
  const input = menuCheckbox(title).querySelector('.el-checkbox__input')
  return {
    checked: input?.classList.contains('is-checked') ?? false,
    indeterminate: input?.classList.contains('is-indeterminate') ?? false
  }
}

const openMenuPermissions = async () => {
  findButton('Menu permissions').click()
  await flushPromises()
}

describe('RolesView permissions', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    mocks.posted.length = 0
    menuTree = [
      {
        menuId: 1,
        title: 'System',
        check: true,
        children: [
          { menuId: 11, title: 'Users', check: true, children: [] },
          { menuId: 12, title: 'Roles', check: false, children: [] }
        ]
      }
    ]
    mocks.GET.mockImplementation((url: string) => {
      if (url === '/sys/role/data-permission/7') {
        return Promise.resolve([DataPermission.BLOG_VIEW_ALL])
      }
      if (url === '/sys/role/menu/7') {
        return Promise.resolve(menuTree)
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

  it('derives a half-checked parent from partial leaf selections', async () => {
    const wrapper = await mountRoles()

    await openMenuPermissions()

    expect(menuCheckboxState('System')).toEqual({
      checked: false,
      indeterminate: true
    })
    expect(menuCheckboxState('Users').checked).toBe(true)
    expect(menuCheckboxState('Roles').checked).toBe(false)

    findButton('Save menu permissions').click()
    await flushPromises()

    expect(mocks.posted).toContainEqual({
      url: '/sys/role/menu/7',
      payload: [11, 1]
    })
    wrapper.unmount()
  })

  it('selects all descendants when checking a parent', async () => {
    const wrapper = await mountRoles()
    await openMenuPermissions()

    menuCheckbox('System').click()
    await flushPromises()

    expect(menuCheckboxState('System').checked).toBe(true)
    expect(menuCheckboxState('Users').checked).toBe(true)
    expect(menuCheckboxState('Roles').checked).toBe(true)

    findButton('Save menu permissions').click()
    await flushPromises()

    expect(mocks.posted).toContainEqual({
      url: '/sys/role/menu/7',
      payload: [1, 11, 12]
    })
    wrapper.unmount()
  })

  it('clears all descendants when unchecking a parent', async () => {
    menuTree[0]!.children.forEach((menu) => {
      menu.check = true
    })
    const wrapper = await mountRoles()
    await openMenuPermissions()

    menuCheckbox('System').click()
    await flushPromises()

    expect(menuCheckboxState('System').checked).toBe(false)
    expect(menuCheckboxState('Users').checked).toBe(false)
    expect(menuCheckboxState('Roles').checked).toBe(false)

    findButton('Save menu permissions').click()
    await flushPromises()

    expect(mocks.posted).toContainEqual({
      url: '/sys/role/menu/7',
      payload: []
    })
    wrapper.unmount()
  })

  it('loads and saves permissions through the dedicated endpoints', async () => {
    const wrapper = await mountRoles()

    findButton('Data permissions').click()
    await flushPromises()

    expect(mocks.GET).toHaveBeenCalledWith('/sys/role/data-permission/7')
    expect(document.body.textContent).toContain('查看全部博客')
    const dataPermissionDialog = Array.from(document.body.querySelectorAll('.el-dialog')).find(
      (dialog) => dialog.querySelector('.el-dialog__title')?.textContent === '数据权限'
    )
    expect(dataPermissionDialog).toBeDefined()
    expect(
      Array.from(dataPermissionDialog!.querySelectorAll('.el-form-item__label')).some(
        (label) => label.textContent?.trim() === '数据权限'
      )
    ).toBe(false)

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
