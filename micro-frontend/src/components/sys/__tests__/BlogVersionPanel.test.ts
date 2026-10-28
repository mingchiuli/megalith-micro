import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ElementPlus from 'element-plus'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import type { BlogVersionSummary, PageAdapter } from '@/type/entity'

const mocks = vi.hoisted(() => ({ GET: vi.fn(), POST: vi.fn() }))

vi.mock('@/http/http', () => ({
  useHttp: () => ({ GET: mocks.GET, POST: mocks.POST })
}))

vi.mock('@/utils/permissions', () => ({
  checkButtonAuth: () => true
}))

import BlogVersionPanel from '@/components/sys/BlogVersionPanel.vue'

const setPanelProps = (
  wrapper: ReturnType<typeof mount>,
  props: Record<string, unknown>
): Promise<void> =>
  (wrapper.setProps as unknown as (nextProps: Record<string, unknown>) => Promise<void>)(props)

const versionPage = (blogId: number): PageAdapter<BlogVersionSummary> => ({
  content: [
    {
      id: blogId * 10,
      blogId,
      versionNo: 1,
      origin: 'DRAFT',
      title: `Article ${blogId}`,
      createdBy: 1,
      createdAt: '2026-10-28 12:00:00'
    }
  ],
  totalElements: 1,
  pageSize: 10,
  pageNumber: 1
})

describe('BlogVersionPanel', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    mocks.GET.mockReset()
    mocks.POST.mockReset()
  })

  it('loads versions when opened and reloads when the article changes', async () => {
    mocks.GET.mockImplementation((url: string) => {
      const blogId = url.includes('/blog/11/') ? 11 : 12
      return Promise.resolve(versionPage(blogId))
    })
    const wrapper = mount(BlogVersionPanel, {
      props: {
        blogId: 11,
        visible: true
      },
      global: { plugins: [ElementPlus] }
    })
    await flushPromises()

    expect(mocks.GET).toHaveBeenCalledWith('/sys/blog/11/versions?currentPage=1&size=10')

    await setPanelProps(wrapper, { blogId: 12 })
    await flushPromises()

    expect(mocks.GET).toHaveBeenLastCalledWith('/sys/blog/12/versions?currentPage=1&size=10')
    expect(mocks.GET).toHaveBeenCalledTimes(2)
    wrapper.unmount()
  })

  it('reloads when the drawer is reopened', async () => {
    mocks.GET.mockResolvedValue(versionPage(11))
    const wrapper = mount(BlogVersionPanel, {
      props: {
        blogId: 11,
        visible: true
      },
      global: { plugins: [ElementPlus] }
    })
    await flushPromises()

    await setPanelProps(wrapper, { visible: false })
    await setPanelProps(wrapper, { visible: true })
    await flushPromises()

    expect(mocks.GET).toHaveBeenCalledTimes(2)
    wrapper.unmount()
  })
})
