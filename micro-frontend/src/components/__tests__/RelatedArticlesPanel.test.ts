import { flushPromises, mount } from '@vue/test-utils'
import { defineComponent, h } from 'vue'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import type { BlogRecommendation } from '@/type/entity'

const mocks = vi.hoisted(() => ({
  GET: vi.fn(),
  routerPush: vi.fn()
}))

vi.mock('@/http/http', () => ({
  useHttp: () => ({ GET: mocks.GET })
}))

vi.mock('vue-router', async (importOriginal) => ({
  ...(await importOriginal<typeof import('vue-router')>()),
  useRouter: () => ({ push: mocks.routerPush })
}))

vi.mock('vue-i18n', () => ({
  useI18n: () => ({ t: (key: string) => key })
}))

import RelatedArticlesPanel from '@/components/RelatedArticlesPanel.vue'

const recommendation = (id: number, title: string): BlogRecommendation => ({
  id,
  title,
  description: '',
  created: '2026-10-26',
  link: '',
  readCount: id
})

const RecommendationCardsStub = defineComponent({
  name: 'RecommendationCards',
  props: {
    items: {
      type: Array as () => BlogRecommendation[],
      required: true
    }
  },
  setup(props) {
    return () =>
      h(
        'div',
        { class: 'recommendation-cards' },
        props.items.map((item) => item.title)
      )
  }
})

describe('RelatedArticlesPanel.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('loads only related articles on an article page', async () => {
    mocks.GET.mockResolvedValueOnce([recommendation(3, 'Related article')])

    const wrapper = mount(RelatedArticlesPanel, {
      props: { blogId: 9, limit: 4 },
      global: {
        stubs: {
          RecommendationCards: RecommendationCardsStub,
          ElSkeleton: {
            template: '<div><slot /></div>'
          }
        }
      }
    })
    await flushPromises()

    const cards = wrapper.findAllComponents(RecommendationCardsStub)
    expect(cards).toHaveLength(1)
    expect(cards[0]!.props('items')).toEqual([recommendation(3, 'Related article')])
    expect(mocks.GET).toHaveBeenCalledWith('/public/blog/related/9?limit=4')
    expect(wrapper.text()).toContain('admin.relatedArticles')
    wrapper.unmount()
  })
})
