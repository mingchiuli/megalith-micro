import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { defineComponent, getCurrentInstance, h, nextTick, reactive, ref } from 'vue'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const route = reactive({
  name: 'blog',
  params: { id: '1' } as Record<string, string>,
  query: {},
  meta: {} as Record<string, unknown>
})

vi.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ push: vi.fn() })
}))

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    locale: ref('zh-CN'),
    t: (key: string) => key
  })
}))

vi.mock('@unhead/vue', () => ({
  useHead: vi.fn()
}))

import App from '@/App.vue'

describe('App route identity', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    route.name = 'blog'
    route.params.id = '1'
    route.meta = {}
  })

  it('remounts the article view when the blog id changes', async () => {
    let mountedViews = 0
    const RouterView = defineComponent({
      setup() {
        mountedViews++
        const instance = getCurrentInstance()
        return () =>
          h('div', {
            class: 'route-view',
            'data-key': String(instance?.vnode.key)
          })
      }
    })
    const Passthrough = defineComponent({
      setup(_, { slots }) {
        return () => h('div', slots.default?.())
      }
    })
    const wrapper = mount(App, {
      global: {
        plugins: [createPinia()],
        stubs: {
          RouterView,
          ElConfigProvider: Passthrough,
          ElDialog: Passthrough,
          ElButton: Passthrough,
          MyFooterItem: true,
          LocaleSwitcher: true
        }
      }
    })

    expect(wrapper.get('.route-view').attributes('data-key')).toBe('1')
    route.params.id = '2'
    await nextTick()

    expect(wrapper.get('.route-view').attributes('data-key')).toBe('2')
    expect(mountedViews).toBe(2)
    wrapper.unmount()
  })
})
