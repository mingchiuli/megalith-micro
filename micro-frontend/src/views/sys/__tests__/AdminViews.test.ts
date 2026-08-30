import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ElementPlus from 'element-plus'
import * as ElIcons from '@element-plus/icons-vue'
import type { PageAdapter } from '@/type/entity'

const mocks = vi.hoisted(() => ({ GET: vi.fn(), POST: vi.fn(), DOWNLOAD: vi.fn(), push: vi.fn() }))

vi.mock('@/http/http', () => ({
  useHttp: () => ({ GET: mocks.GET, POST: mocks.POST, DOWNLOAD: mocks.DOWNLOAD })
}))
vi.mock('@/utils/permissions', () => ({
  checkButtonAuth: () => false,
  getButtonType: () => '',
  getButtonTitle: (name: string) => name
}))
vi.mock('vue-router', async (importOriginal) => ({
  ...(await importOriginal<typeof import('vue-router')>()),
  useRouter: () => ({ push: mocks.push })
}))

import AuthorityView from '@/views/sys/AuthorityView.vue'
import BlogsView from '@/views/sys/BlogsView.vue'
import MenusView from '@/views/sys/MenusView.vue'
import UsersView from '@/views/sys/UsersView.vue'

const emptyPage: PageAdapter<never> = {
  content: [],
  totalElements: 0,
  pageSize: 5,
  pageNumber: 1
}

const mountView = async (component: typeof UsersView) => {
  const wrapper = mount(component, {
    global: { plugins: [createPinia(), ElementPlus], components: { ...ElIcons } }
  })
  await flushPromises()
  return wrapper
}

describe('admin list views', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    mocks.GET.mockImplementation((url: string) => {
      if (url.startsWith('/sys/user/page/') || url.startsWith('/sys/blog/blogs')) {
        return Promise.resolve(emptyPage)
      }
      return Promise.resolve([])
    })
  })

  afterEach(() => vi.clearAllMocks())

  it.each([
    ['users', UsersView, '/sys/user/page/1?size=5'],
    ['authorities', AuthorityView, '/sys/authority/list'],
    ['menus', MenusView, '/sys/menu/list'],
    ['blogs', BlogsView, '/sys/blog/blogs?currentPage=1&size=5']
  ])('loads the %s list through its dedicated endpoint', async (_, view, endpoint) => {
    const wrapper = await mountView(view)
    expect(mocks.GET).toHaveBeenCalledWith(endpoint)
    wrapper.unmount()
  })
})
