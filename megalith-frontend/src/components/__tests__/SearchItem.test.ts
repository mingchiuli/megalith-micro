import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ElementPlus from 'element-plus'
import * as ElIcons from '@element-plus/icons-vue'

vi.mock('@/router', () => ({
  default: { push: vi.fn(), hasRoute: vi.fn(() => false), removeRoute: vi.fn() }
}))

vi.mock('@/http/http', () => ({
  GET: vi.fn(),
  POST: vi.fn(),
  DOWNLOAD: vi.fn(),
  UPLOAD: vi.fn()
}))

vi.mock('@/http/axios', () => ({
  httpClient: { get: vi.fn(), post: vi.fn() },
  longHttpClient: { get: vi.fn(), post: vi.fn() },
  aiHttpClient: { get: vi.fn(), post: vi.fn() }
}))

vi.mock('@/config/otel', () => ({ createTraceParent: vi.fn(() => 'tp') }))

// HotItem 子组件存根：仅暴露 load 方法
vi.mock('@/components/HotItem.vue', () => ({
  default: {
    name: 'HotItem',
    setup(_: unknown, { expose }: { expose: (api: Record<string, unknown>) => void }) {
      expose({ load: vi.fn() })
      return () => null
    }
  }
}))

import SearchItem from '@/components/SearchItem.vue'
import { GET } from '@/http/http'
import type { SearchPage, BlogDesc } from '@/type/entity'

const buildPage = (content: BlogDesc[]): SearchPage<BlogDesc> => ({
  content,
  pageSize: 10,
  totalElements: content.length,
  pageNumber: 1,
  additional: null
})

const mountSearch = () =>
  mount(SearchItem, {
    attachTo: document.body,
    props: {
      loading: false,
      searchDialogVisible: true
    },
    global: {
      plugins: [ElementPlus],
      components: { ...ElIcons }
    }
  })

describe('SearchItem.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  afterEach(() => {
    document.body.innerHTML = ''
  })

  it('挂载后通过 Teleport 渲染 dialog 内容', async () => {
    const wrapper = mountSearch()
    // el-dialog 使用 Teleport + 异步渲染，需要等微任务队列清空
    await flushPromises()
    expect(document.body.textContent).toContain('Confirm')
    wrapper.unmount()
  })

  it('searchAllInfo 命中结果时 emit transSearchData', async () => {
    const desc = { id: 1, title: 't', description: 'd', created: '', link: '', status: 0 } as BlogDesc
    vi.mocked(GET).mockResolvedValueOnce(buildPage([desc]) as never)

    const wrapper = mountSearch()
    const exposed = wrapper.vm as unknown as {
      searchAllInfo: (q: string, p?: number) => Promise<void>
    }
    await exposed.searchAllInfo('vue', 1)

    expect(GET).toHaveBeenCalledTimes(1)
    expect(wrapper.emitted('transSearchData')).toBeTruthy()
    const payload = wrapper.emitted('transSearchData')![0]![0] as SearchPage<BlogDesc>
    expect(payload.content).toHaveLength(1)
    expect(wrapper.emitted('refresh')).toBeFalsy()
    wrapper.unmount()
  })

  it('searchAllInfo 无结果时 emit refresh', async () => {
    vi.mocked(GET).mockResolvedValueOnce(buildPage([]) as never)

    const wrapper = mountSearch()
    const exposed = wrapper.vm as unknown as {
      searchAllInfo: (q: string, p?: number) => Promise<void>
    }
    await exposed.searchAllInfo('none', 1)

    expect(wrapper.emitted('refresh')).toBeTruthy()
    expect(wrapper.emitted('transSearchData')).toBeFalsy()
    wrapper.unmount()
  })

  it('空查询直接 emit refresh，不发起请求', async () => {
    const wrapper = mountSearch()
    const exposed = wrapper.vm as unknown as {
      searchAllInfo: (q: string, p?: number) => Promise<void>
    }
    await exposed.searchAllInfo('', 1)

    expect(GET).not.toHaveBeenCalled()
    expect(wrapper.emitted('refresh')).toBeTruthy()
    wrapper.unmount()
  })

  it('searchAllInfo 命中结果后会关闭 dialog（更新 v-model）', async () => {
    const desc = { id: 1, title: 't', description: 'd', created: '', link: '', status: 0 } as BlogDesc
    vi.mocked(GET).mockResolvedValueOnce(buildPage([desc]) as never)

    const wrapper = mountSearch()
    const exposed = wrapper.vm as unknown as {
      searchAllInfo: (q: string, p?: number) => Promise<void>
    }
    await exposed.searchAllInfo('vue', 1)

    const updates = wrapper.emitted('update:searchDialogVisible')
    expect(updates?.[0]?.[0]).toBe(false)
    wrapper.unmount()
  })
})
