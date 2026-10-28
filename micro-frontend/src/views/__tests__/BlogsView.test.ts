import { flushPromises, mount, type VueWrapper } from '@vue/test-utils'
import { createPinia, setActivePinia, type Pinia } from 'pinia'
import { defineComponent, h, nextTick } from 'vue'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import type { BlogDesc, BlogRecommendation, PageAdapter, SearchPage } from '@/type/entity'
import { Status } from '@/type/entity'
import { ssrDataStore } from '@/stores'

const mocks = vi.hoisted(() => ({
  GET: vi.fn(),
  routerPush: vi.fn(),
  routeQuery: {} as Record<string, string>,
  searchAllInfo: vi.fn(),
  cancelSearch: vi.fn()
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
  props: { timestamp: { type: String, default: '' } },
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
  emits: [
    'update:loading',
    'update:searchDialogVisible',
    'transSearchData',
    'refresh',
    'searchError'
  ],
  setup(_, { expose }) {
    expose({
      searchAllInfo: mocks.searchAllInfo,
      cancelSearch: mocks.cancelSearch
    })
    return () => h('div', { class: 'search-stub' })
  }
})

const FooterStub = defineComponent({
  props: {
    loading: Boolean,
    hasMore: Boolean,
    error: Boolean
  },
  emits: ['load', 'retry'],
  setup(_, { emit }) {
    return () =>
      h('div', { class: 'footer-stub' }, [
        h('button', { class: 'load-more', onClick: () => emit('load') }, 'load'),
        h('button', { class: 'retry-feed', onClick: () => emit('retry') }, 'retry')
      ])
  }
})

const PassThroughStub = defineComponent({
  setup(_, { slots }) {
    return () => h('div', slots.default?.())
  }
})

const componentStubs = {
  ReadTokenItem: true,
  InfiniteScrollFooter: FooterStub,
  SearchItem: SearchStub,
  ElButton: PassThroughStub,
  ElCard: PassThroughStub,
  ElImage: ImageStub,
  ElLink: PassThroughStub,
  ElSkeleton: SkeletonStub,
  ElText: PassThroughStub,
  ElTimeline: PassThroughStub,
  ElTimelineItem: TimelineItemStub
}

const recommendation = (id: number): BlogRecommendation => ({
  id,
  title: `Recommendation ${id}`,
  description: `Description ${id}`,
  created: '2026-10-25',
  link: '',
  readCount: id
})

const blog = (id: number, link = ''): BlogDesc => ({
  id,
  title: `Blog ${id}`,
  description: `Description ${id}`,
  created: '2026-10-25',
  link,
  status: Status.NORMAL
})

const recommendationPage = (
  content: BlogRecommendation[],
  pageNumber = 1,
  last = true
): PageAdapter<BlogRecommendation> => ({
  content,
  totalElements: content.length,
  pageSize: 5,
  pageNumber,
  last
})

const searchPage = (content: BlogDesc[], pageNumber = 1, last = true): SearchPage<BlogDesc> => ({
  content,
  totalElements: content.length,
  pageSize: 5,
  pageNumber,
  last,
  additional: null
})

const mountBlogs = (pinia: Pinia = createPinia()): VueWrapper => {
  setActivePinia(pinia)
  return mount(BlogsView, {
    global: {
      plugins: [pinia],
      stubs: componentStubs
    }
  })
}

const cards = (wrapper: VueWrapper) => wrapper.findAll('.blog-item .title')

describe('BlogsView feed', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mocks.routeQuery = {}
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('shows paginated recommendations below the admin link without page controls', async () => {
    mocks.GET.mockResolvedValueOnce(recommendationPage([recommendation(1)], 1, false))
    const wrapper = mountBlogs()
    await flushPromises()

    expect(mocks.GET).toHaveBeenCalledWith('/public/blog/recommendations/page/1')
    expect(cards(wrapper).map((card) => card.text())).toEqual(['Recommendation 1'])
    expect(wrapper.find('.el-pagination').exists()).toBe(false)
    expect(wrapper.html().indexOf('进入后台')).toBeLessThan(
      wrapper.html().indexOf('Recommendation 1')
    )
    expect(wrapper.text()).not.toContain('推荐阅读')
    wrapper.unmount()
  })

  it('renders recommendations with the article card layout', async () => {
    mocks.GET.mockResolvedValueOnce(
      recommendationPage([{ ...recommendation(1), link: 'https://cdn.example/1.png' }])
    )
    const wrapper = mountBlogs()
    await flushPromises()

    const items = wrapper.findAllComponents(TimelineItemStub)
    expect(items.map((item) => item.props('timestamp'))).toEqual(['2026-10-25'])
    expect(wrapper.find('.cover-image').attributes('src')).toBe('https://cdn.example/1.png')
    expect(wrapper.find('.blog-item').text()).toContain('Description 1')
    wrapper.unmount()
  })

  it('appends and de-duplicates recommendation pages', async () => {
    mocks.GET.mockResolvedValueOnce(
      recommendationPage([recommendation(1), recommendation(2)], 1, false)
    ).mockResolvedValueOnce(recommendationPage([recommendation(2), recommendation(3)], 2, true))
    const wrapper = mountBlogs()
    await flushPromises()

    await wrapper.get('.load-more').trigger('click')
    await flushPromises()

    expect(mocks.GET).toHaveBeenLastCalledWith('/public/blog/recommendations/page/2')
    expect(cards(wrapper).map((card) => card.text())).toEqual([
      'Recommendation 1',
      'Recommendation 2',
      'Recommendation 3'
    ])
    wrapper.unmount()
  })

  it('replaces recommendations with search results and keeps an empty search empty', async () => {
    mocks.routeQuery = { q: 'vue' }
    mocks.GET.mockResolvedValueOnce(searchPage([blog(9)]))
    const wrapper = mountBlogs()
    await flushPromises()

    expect(mocks.GET).toHaveBeenCalledWith(expect.stringContaining('/search/public/blog?'))
    expect(cards(wrapper).map((card) => card.text())).toEqual(['Blog 9'])

    wrapper.findComponent(SearchStub).vm.$emit('transSearchData', searchPage([], 1, true))
    await nextTick()

    expect(cards(wrapper)).toHaveLength(0)
    expect(wrapper.text()).toContain('未找到相关文章')
    expect(mocks.GET).toHaveBeenCalledTimes(1)
    wrapper.unmount()
  })

  it('continues the active search feed when the footer requests more', async () => {
    mocks.routeQuery = { q: 'vue' }
    mocks.GET.mockResolvedValueOnce(searchPage([blog(1)], 1, false))
    const wrapper = mountBlogs()
    await flushPromises()

    await wrapper.get('.load-more').trigger('click')
    await nextTick()
    expect(mocks.searchAllInfo).toHaveBeenCalledWith('vue', 2)

    wrapper
      .findComponent(SearchStub)
      .vm.$emit('transSearchData', searchPage([blog(1), blog(2)], 2, true))
    await nextTick()

    expect(wrapper.findAll('.blog-item')).toHaveLength(2)
    wrapper.unmount()
  })

  it('uses hydrated SSR data without requesting the first recommendation page again', async () => {
    const pinia = createPinia()
    setActivePinia(pinia)
    ssrDataStore(pinia).set('blog-feed:recommendations:1', {
      kind: 'recommendations',
      page: recommendationPage([recommendation(1)])
    })

    const wrapper = mountBlogs(pinia)
    await nextTick()

    expect(mocks.GET).not.toHaveBeenCalled()
    expect(cards(wrapper).map((card) => card.text())).toEqual(['Recommendation 1'])
    wrapper.unmount()
  })

  it('keeps loaded content when the next recommendation page fails and retries it', async () => {
    mocks.GET.mockResolvedValueOnce(recommendationPage([recommendation(1)], 1, false))
      .mockRejectedValueOnce(new Error('offline'))
      .mockResolvedValueOnce(recommendationPage([recommendation(2)], 2, true))
    const wrapper = mountBlogs()
    await flushPromises()

    await wrapper.get('.load-more').trigger('click')
    await flushPromises()

    expect(cards(wrapper)).toHaveLength(1)
    expect(wrapper.findComponent(FooterStub).props('error')).toBe(true)

    await wrapper.get('.retry-feed').trigger('click')
    await flushPromises()

    expect(mocks.GET).toHaveBeenLastCalledWith('/public/blog/recommendations/page/2')
    expect(cards(wrapper)).toHaveLength(2)
    wrapper.unmount()
  })

  it('returns to recommendation page one when search is cleared', async () => {
    mocks.routeQuery = { q: 'vue' }
    mocks.GET.mockResolvedValueOnce(searchPage([blog(9)])).mockResolvedValueOnce(
      recommendationPage([recommendation(1)])
    )
    const wrapper = mountBlogs()
    await flushPromises()

    wrapper.findComponent(SearchStub).vm.$emit('refresh')
    await flushPromises()

    expect(mocks.cancelSearch).toHaveBeenCalledOnce()
    expect(mocks.GET).toHaveBeenLastCalledWith('/public/blog/recommendations/page/1')
    expect(cards(wrapper)).toHaveLength(1)
    wrapper.unmount()
  })
})
