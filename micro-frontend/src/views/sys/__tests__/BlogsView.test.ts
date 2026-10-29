import { flushPromises, mount, type VueWrapper } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ElementPlus, { ElTable } from 'element-plus'
import { defineComponent, h } from 'vue'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { ButtonAuth, Status, type BlogSys, type PageAdapter } from '@/type/entity'

const mocks = vi.hoisted(() => ({
  GET: vi.fn(),
  POST: vi.fn(),
  DOWNLOAD: vi.fn(),
  push: vi.fn(),
  allowed: new Set<string>()
}))

vi.mock('@/http/http', () => ({
  useHttp: () => ({ GET: mocks.GET, POST: mocks.POST, DOWNLOAD: mocks.DOWNLOAD })
}))

vi.mock('@/utils/permissions', () => ({
  checkButtonAuth: (name: string) => mocks.allowed.has(name),
  getButtonType: () => 'primary',
  getButtonTitle: (name: string) => name
}))

vi.mock('vue-router', async (importOriginal) => ({
  ...(await importOriginal<typeof import('vue-router')>()),
  useRouter: () => ({ push: mocks.push })
}))

import BlogsView from '@/views/sys/BlogsView.vue'

const VersionPanelStub = defineComponent({
  props: {
    visible: Boolean,
    blogId: Number
  },
  emits: ['update:visible'],
  setup(props) {
    return () =>
      h('div', {
        class: 'version-panel-stub',
        'data-blog-id': String(props.blogId),
        'data-visible': String(props.visible)
      })
  }
})

const blog = (status: Status): BlogSys => ({
  id: 7,
  title: 'Article',
  description: 'Description',
  created: '2026-10-28 12:00:00',
  updated: '2026-10-28 12:00:00',
  content: 'Content',
  readCount: 1,
  recentReadCount: 1,
  status,
  hasDraft: false,
  draftNewer: false,
  link: ''
})

const blogPage = (status: Status): PageAdapter<BlogSys> => ({
  content: [blog(status)],
  totalElements: 1,
  pageSize: 5,
  pageNumber: 1
})

const mountView = async (): Promise<VueWrapper> => {
  const pinia = createPinia()
  setActivePinia(pinia)
  const wrapper = mount(BlogsView, {
    global: {
      plugins: [pinia, ElementPlus],
      stubs: { BlogVersionPanel: VersionPanelStub }
    }
  })
  await flushPromises()
  return wrapper
}

const buttonWithText = (wrapper: VueWrapper, text: string) =>
  wrapper.findAll('button').find((button) => button.text() === text)

describe('admin BlogsView row actions', () => {
  beforeEach(() => {
    mocks.allowed = new Set([
      ButtonAuth.SYS_EDIT_VERSIONS,
      ButtonAuth.SYS_EDIT_STATUS,
      ButtonAuth.SYS_BLOG_PASSWORD,
      ButtonAuth.SYS_BLOG_DELETE
    ])
    mocks.POST.mockResolvedValue(null)
  })

  afterEach(() => vi.clearAllMocks())

  it('opens the version history for the selected article', async () => {
    mocks.GET.mockResolvedValue(blogPage(Status.NORMAL))
    const wrapper = await mountView()

    await buttonWithText(wrapper, '版本历史')!.trigger('click')
    await flushPromises()

    const panel = wrapper.getComponent(VersionPanelStub)
    expect(panel.props('blogId')).toBe(7)
    expect(panel.props('visible')).toBe(true)
    expect(buttonWithText(wrapper, '版本历史')!.classes()).toContain('el-button--info')
    wrapper.unmount()
  })

  it('allows resizing the operation column at the end of the table', async () => {
    mocks.GET.mockResolvedValue(blogPage(Status.NORMAL))
    const wrapper = await mountView()

    expect(wrapper.findComponent(ElTable).props()).toMatchObject({ allowDragLastColumn: true })
    wrapper.unmount()
  })

  it('takes a public article offline and refreshes the list', async () => {
    mocks.GET.mockResolvedValue(blogPage(Status.NORMAL))
    const wrapper = await mountView()
    const initialRequests = mocks.GET.mock.calls.length

    await buttonWithText(wrapper, '下线')!.trigger('click')
    await flushPromises()

    expect(mocks.POST).toHaveBeenCalledWith('/sys/blog/7/offline', {})
    expect(buttonWithText(wrapper, '下线')!.classes()).toContain('el-button--warning')
    expect(mocks.GET.mock.calls.length).toBeGreaterThan(initialRequests)
    wrapper.unmount()
  })

  it('brings a hidden article back online', async () => {
    mocks.GET.mockResolvedValue(blogPage(Status.BLOCK))
    const wrapper = await mountView()

    await buttonWithText(wrapper, '重新上线')!.trigger('click')
    await flushPromises()

    expect(mocks.POST).toHaveBeenCalledWith('/sys/blog/7/online', {})
    expect(buttonWithText(wrapper, '重新上线')!.classes()).toContain('el-button--success')
    wrapper.unmount()
  })

  it('does not offer status changes for draft articles', async () => {
    mocks.GET.mockResolvedValue(blogPage(Status.DRAFT))
    const wrapper = await mountView()

    expect(buttonWithText(wrapper, '下线')).toBeUndefined()
    expect(buttonWithText(wrapper, '重新上线')).toBeUndefined()
    expect(buttonWithText(wrapper, '版本历史')).toBeDefined()
    wrapper.unmount()
  })
})
