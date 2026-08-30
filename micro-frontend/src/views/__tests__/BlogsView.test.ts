import { flushPromises, mount, type VueWrapper } from '@vue/test-utils'
import { createPinia, setActivePinia, type Pinia } from 'pinia'
import { defineComponent, h, nextTick } from 'vue'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import type { BlogDesc, PageAdapter } from '@/type/entity'
import { Status } from '@/type/entity'
import { ssrDataStore } from '@/stores'

const mocks = vi.hoisted(() => ({
  GET: vi.fn(),
  routerPush: vi.fn(),
  routeQuery: {} as Record<string, string>
}))

vi.mock('@/http/http', () => ({
  useHttp: () => ({ GET: mocks.GET })
}))

vi.mock('vue-router', async (importOriginal) => ({
  ...(await importOriginal<typeof import('vue-router')>()),
  useRouter: () => ({ push: mocks.routerPush }),
  useRoute: () => ({ query: mocks.routeQuery })
}))

import BlogsView from '@/views/BlogsView.vue'

const SkeletonStub = defineComponent({
  inheritAttrs: false,
  props: { loading: { type: Boolean, default: true } },
  setup(props, { attrs, slots }) {
    return () =>
      h(
        'div',
        { ...attrs, 'data-loading': String(props.loading) },
        props.loading ? slots.template?.() : slots.default?.()
      )
  }
})

const TimelineItemStub = defineComponent({
  inheritAttrs: false,
  setup(_, { attrs, slots }) {
    return () => h('article', { ...attrs, class: ['blog-item', attrs.class] }, slots.default?.())
  }
})

const ImageStub = defineComponent({
  props: { src: { type: String, required: true } },
  emits: ['load', 'error'],
  setup(props, { emit }) {
    return () =>
      h('img', {
        class: 'cover-image',
        src: props.src,
        onLoad: () => emit('load'),
        onError: () => emit('error')
      })
  }
})

const SearchStub = defineComponent({
  props: {
    loading: Boolean,
    searchDialogVisible: Boolean
  },
  emits: ['update:loading', 'update:searchDialogVisible', 'transSearchData', 'refresh'],
  setup(_, { expose }) {
    expose({ searchAllInfo: vi.fn() })
    return () => h('div')
  }
})

const PassThroughStub = defineComponent({
  setup(_, { slots }) {
    return () => h('div', slots.default?.())
  }
})

const componentStubs = {
  ReadTokenItem: true,
  SearchItem: SearchStub,
  ElButton: PassThroughStub,
  ElCard: PassThroughStub,
  ElImage: ImageStub,
  ElLink: PassThroughStub,
  ElPagination: true,
  ElSkeleton: SkeletonStub,
  ElText: PassThroughStub,
  ElTimeline: PassThroughStub,
  ElTimelineItem: TimelineItemStub
}

const blog = (id: number, link = ''): BlogDesc => ({
  id,
  title: `Blog ${id}`,
  description: `Description ${id}`,
  created: '2026-08-30',
  link,
  status: Status.NORMAL
})

const page = (content: BlogDesc[]): PageAdapter<BlogDesc> => ({
  content,
  totalElements: content.length,
  pageSize: 5,
  pageNumber: 1
})

const deferred = <T>() => {
  let resolve!: (value: T) => void
  let reject!: (cause: unknown) => void
  const promise = new Promise<T>((resolvePromise, rejectPromise) => {
    resolve = resolvePromise
    reject = rejectPromise
  })
  return { promise, resolve, reject }
}

const mountBlogs = (pinia: Pinia = createPinia()): VueWrapper => {
  setActivePinia(pinia)
  return mount(BlogsView, {
    global: {
      plugins: [pinia],
      stubs: componentStubs
    }
  })
}

const skeletonLoading = (wrapper: VueWrapper) =>
  wrapper.get('.blogs-skeleton').attributes('data-loading') === 'true'

const blogItemsHidden = (wrapper: VueWrapper) =>
  wrapper.findAll('.blog-item').every((item) => item.attributes('style')?.includes('display: none'))

describe('BlogsView loading', () => {
  beforeEach(() => {
    vi.useRealTimers()
    vi.clearAllMocks()
    mocks.routeQuery = {}
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('keeps every blog hidden until all cover images settle', async () => {
    const request = deferred<PageAdapter<BlogDesc>>()
    mocks.GET.mockReturnValueOnce(request.promise)
    const wrapper = mountBlogs()

    expect(skeletonLoading(wrapper)).toBe(true)
    request.resolve(page([blog(1, '/cover-1.webp'), blog(2, '/cover-2.webp')]))
    await flushPromises()

    expect(skeletonLoading(wrapper)).toBe(true)
    expect(blogItemsHidden(wrapper)).toBe(true)

    await wrapper.findAll('.cover-image')[0]!.trigger('load')
    expect(skeletonLoading(wrapper)).toBe(true)

    await wrapper.findAll('.cover-image')[1]!.trigger('error')
    expect(skeletonLoading(wrapper)).toBe(false)
    expect(blogItemsHidden(wrapper)).toBe(false)
    wrapper.unmount()
  })

  it('shows a page without cover images as soon as its request completes', async () => {
    mocks.GET.mockResolvedValueOnce(page([blog(1), blog(2)]))
    const wrapper = mountBlogs()
    await flushPromises()

    expect(skeletonLoading(wrapper)).toBe(false)
    expect(blogItemsHidden(wrapper)).toBe(false)
    wrapper.unmount()
  })

  it('uses hydrated SSR data without another request and still waits for its covers', async () => {
    const pinia = createPinia()
    setActivePinia(pinia)
    ssrDataStore(pinia).set('blogs::1', page([blog(1, '/hydrated.webp')]))

    const wrapper = mountBlogs(pinia)
    await nextTick()

    expect(mocks.GET).not.toHaveBeenCalled()
    expect(skeletonLoading(wrapper)).toBe(true)
    await wrapper.get('.cover-image').trigger('load')
    expect(skeletonLoading(wrapper)).toBe(false)
    wrapper.unmount()
  })

  it('keeps initial search results behind the skeleton until their covers settle', async () => {
    mocks.routeQuery = { q: 'vue' }
    mocks.GET.mockResolvedValueOnce(page([blog(1, '/search.webp')]))
    const wrapper = mountBlogs()
    await flushPromises()

    expect(mocks.GET).toHaveBeenCalledWith(expect.stringContaining('/search/public/blog?'))
    expect(skeletonLoading(wrapper)).toBe(true)
    await wrapper.get('.cover-image').trigger('load')
    expect(skeletonLoading(wrapper)).toBe(false)
    wrapper.unmount()
  })

  it('releases the skeleton when the initial request fails', async () => {
    mocks.GET.mockRejectedValueOnce(new Error('request failed'))
    const wrapper = mountBlogs()
    await flushPromises()

    expect(skeletonLoading(wrapper)).toBe(false)
    wrapper.unmount()
  })

  it('releases a stalled image batch after ten seconds', async () => {
    vi.useFakeTimers()
    mocks.GET.mockResolvedValueOnce(page([blog(1, '/stalled.webp')]))
    const wrapper = mountBlogs()
    await Promise.resolve()
    await nextTick()
    await Promise.resolve()
    await nextTick()

    expect(skeletonLoading(wrapper)).toBe(true)
    await vi.advanceTimersByTimeAsync(10_000)
    await nextTick()

    expect(skeletonLoading(wrapper)).toBe(false)
    wrapper.unmount()
  })
})
