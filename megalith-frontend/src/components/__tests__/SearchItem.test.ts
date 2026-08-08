import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import type { Stubs } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import * as ElIcons from '@element-plus/icons-vue'

const mocks = vi.hoisted(() => ({ GET: vi.fn(), routerPush: vi.fn() }))

vi.mock('@/http/http', () => ({
  useHttp: () => ({ GET: mocks.GET })
}))

vi.mock('vue-router', async (importOriginal) => ({
  ...(await importOriginal<typeof import('vue-router')>()),
  useRouter: () => ({ push: mocks.routerPush })
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
import type { SearchPage, BlogDesc } from '@/type/entity'
import { blogsStore } from '@/stores'

const buildPage = (content: BlogDesc[]): SearchPage<BlogDesc> => ({
  content,
  pageSize: 10,
  totalElements: content.length,
  pageNumber: 1,
  additional: null
})

const mountSearch = (stubs: Stubs = {}) =>
  mount(SearchItem, {
    attachTo: document.body,
    props: {
      loading: false,
      searchDialogVisible: true
    },
    global: {
      plugins: [ElementPlus],
      components: { ...ElIcons },
      stubs
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
    expect(document.body.textContent).toContain('确定')
    wrapper.unmount()
  })

  it('searchAllInfo 命中结果时 emit transSearchData', async () => {
    const desc = {
      id: 1,
      title: 't',
      description: 'd',
      created: '',
      link: '',
      status: 0
    } as BlogDesc
    mocks.GET.mockResolvedValueOnce(buildPage([desc]))

    const wrapper = mountSearch()
    const exposed = wrapper.vm as unknown as {
      searchAllInfo: (q: string, p?: number) => Promise<void>
    }
    await exposed.searchAllInfo('vue', 1)

    expect(mocks.GET).toHaveBeenCalledTimes(1)
    expect(wrapper.emitted('transSearchData')).toBeTruthy()
    const payload = wrapper.emitted('transSearchData')![0]![0] as SearchPage<BlogDesc>
    expect(payload.content).toHaveLength(1)
    expect(wrapper.emitted('refresh')).toBeFalsy()
    wrapper.unmount()
  })

  it('searchAllInfo 无结果时 emit refresh', async () => {
    mocks.GET.mockResolvedValueOnce(buildPage([]))

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

    expect(mocks.GET).not.toHaveBeenCalled()
    expect(wrapper.emitted('refresh')).toBeTruthy()
    wrapper.unmount()
  })

  it('searchAllInfo 命中结果后会关闭 dialog（更新 v-model）', async () => {
    const desc = {
      id: 1,
      title: 't',
      description: 'd',
      created: '',
      link: '',
      status: 0
    } as BlogDesc
    mocks.GET.mockResolvedValueOnce(buildPage([desc]))

    const wrapper = mountSearch()
    const exposed = wrapper.vm as unknown as {
      searchAllInfo: (q: string, p?: number) => Promise<void>
    }
    await exposed.searchAllInfo('vue', 1)

    const updates = wrapper.emitted('update:searchDialogVisible')
    expect(updates?.[0]?.[0]).toBe(false)
    wrapper.unmount()
  })

  it('新建议请求会取消旧请求且旧响应不会覆盖结果', async () => {
    type Pending = { resolve: (page: SearchPage<BlogDesc>) => void }
    const pending: Pending[] = []
    mocks.GET.mockImplementation(
      (_url: string, config?: { signal?: AbortSignal }) =>
        new Promise<SearchPage<BlogDesc>>((resolve, reject) => {
          pending.push({ resolve })
          config?.signal?.addEventListener('abort', () =>
            reject(new DOMException('aborted', 'AbortError'))
          )
        })
    )
    const wrapper = mountSearch()
    const exposed = wrapper.vm as unknown as {
      searchAbstractAsync: (query: string, cb: (items: BlogDesc[]) => void) => void
    }
    const oldCallback = vi.fn()
    const newCallback = vi.fn()

    exposed.searchAbstractAsync('old', oldCallback)
    exposed.searchAbstractAsync('new', newCallback)
    const result = {
      id: 2,
      title: 'new',
      description: 'd',
      created: '',
      link: '',
      status: 0
    } as BlogDesc
    pending[1]!.resolve(buildPage([result]))
    await flushPromises()

    expect(oldCallback).not.toHaveBeenCalled()
    expect(newCallback).toHaveBeenCalledOnce()
    expect(newCallback.mock.calls[0]![0]![0]!.value).toBe('new')
    wrapper.unmount()
  })

  it('空建议和失败建议都返回空数组', async () => {
    mocks.GET.mockRejectedValueOnce(new Error('search failed'))
    const wrapper = mountSearch()
    const exposed = wrapper.vm as unknown as {
      searchAbstractAsync: (query: string, cb: (items: BlogDesc[]) => void) => void
    }
    const emptyCallback = vi.fn()
    const failedCallback = vi.fn()

    exposed.searchAbstractAsync('   ', emptyCallback)
    exposed.searchAbstractAsync('failed', failedCallback)
    await flushPromises()

    expect(emptyCallback).toHaveBeenCalledWith([])
    expect(failedCallback).toHaveBeenCalledWith([])
    wrapper.unmount()
  })

  it('handles selection, close, clear and failed full search states', async () => {
    mocks.GET.mockRejectedValueOnce(new Error('search failed'))
    const wrapper = mountSearch()
    const exposed = wrapper.vm as unknown as {
      handleSelect: (item: { id: number }) => void
      searchBeforeClose: (close: () => void) => void
      clearSearch: () => void
      searchAllInfo: (query: string) => Promise<void>
      highlighted: (label: string, html: string) => string
    }
    const close = vi.fn()
    blogsStore().keywords = 'value'

    exposed.handleSelect({ id: 9 })
    exposed.clearSearch()
    exposed.searchBeforeClose(close)
    await exposed.searchAllInfo('failed')

    expect(mocks.routerPush).toHaveBeenCalledWith({ name: 'blog', params: { id: 9 } })
    expect(blogsStore().keywords).toBe('')
    expect(close).toHaveBeenCalledOnce()
    expect(wrapper.emitted('refresh')).toBeTruthy()
    expect(wrapper.emitted('update:loading')?.at(-1)?.[0]).toBe(false)
    expect(exposed.highlighted('Title:', '<em>safe</em><script>x</script>')).not.toContain(
      '<script>'
    )
    wrapper.unmount()
  })

  it('renders every highlighted suggestion field and loading slot', async () => {
    const autocomplete = defineComponent({
      setup(_, { slots }) {
        const item = {
          highlight: {
            title: ['<em>title</em>'],
            description: ['<em>description</em>'],
            content: ['<em>content</em>']
          }
        }
        return () => h('div', [slots.default?.({ item }), slots.loading?.()])
      }
    })
    const wrapper = mountSearch({ 'el-autocomplete': autocomplete })
    await flushPromises()

    expect(document.body.innerHTML).toContain('title')
    expect(document.body.innerHTML).toContain('description')
    expect(document.body.innerHTML).toContain('content')
    expect(document.body.querySelector('svg.circular')).not.toBeNull()
    wrapper.unmount()
  })
})
