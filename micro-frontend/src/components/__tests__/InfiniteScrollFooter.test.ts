import { flushPromises, mount, type VueWrapper } from '@vue/test-utils'
import { nextTick, type ComponentPublicInstance } from 'vue'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import InfiniteScrollFooter from '@/components/InfiniteScrollFooter.vue'

let intersectionCallback: IntersectionObserverCallback

type FooterProps = {
  loading: boolean
  hasMore: boolean
  error: boolean
  loadingText: string
  errorText: string
  retryText: string
  endText: string
}

class IntersectionObserverStub {
  constructor(callback: IntersectionObserverCallback) {
    intersectionCallback = callback
  }

  observe() {}

  unobserve() {}

  disconnect() {}
}

// setProps 需要宽松的 props 类型，否则 vue-tsc 推断不出 SFC 的 props
const mountFooter = (
  overrides: Partial<FooterProps> = {}
): VueWrapper<ComponentPublicInstance<Record<string, unknown>>> =>
  mount(InfiniteScrollFooter, {
    props: {
      loading: false,
      hasMore: true,
      error: false,
      loadingText: 'Loading',
      errorText: 'Failed',
      retryText: 'Retry',
      endText: 'End',
      ...overrides
    },
    global: {
      stubs: {
        ElLink: {
          template: '<button @click="$emit(\'click\')"><slot /></button>'
        }
      }
    }
  }) as VueWrapper<ComponentPublicInstance<Record<string, unknown>>>

describe('InfiniteScrollFooter.vue', () => {
  beforeEach(() => {
    vi.stubGlobal('IntersectionObserver', IntersectionObserverStub)
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('requests another batch when the sentinel becomes visible', async () => {
    const wrapper = mountFooter()
    intersectionCallback([{ isIntersecting: true } as IntersectionObserverEntry], {} as never)
    expect(wrapper.emitted('load')).toHaveLength(1)
    wrapper.unmount()

    const loadingWrapper = mountFooter({ loading: true })
    intersectionCallback([{ isIntersecting: true } as IntersectionObserverEntry], {} as never)
    expect(loadingWrapper.emitted('load')).toBeUndefined()
    expect(loadingWrapper.text()).toContain('Loading')
    loadingWrapper.unmount()
  })

  it('waits for the sentinel to re-enter the viewport after a finished load', async () => {
    const wrapper = mountFooter()
    intersectionCallback([{ isIntersecting: true } as IntersectionObserverEntry], {} as never)
    expect(wrapper.emitted('load')).toHaveLength(1)

    await wrapper.setProps({ loading: true })
    await wrapper.setProps({ loading: false })
    await flushPromises()
    await nextTick()

    expect(wrapper.emitted('load')).toHaveLength(1)

    intersectionCallback([{ isIntersecting: true } as IntersectionObserverEntry], {} as never)
    expect(wrapper.emitted('load')).toHaveLength(2)
    wrapper.unmount()
  })

  it('shows retry for failures and an end message when exhausted', async () => {
    const errorWrapper = mountFooter({ error: true })
    expect(errorWrapper.text()).toContain('Failed')
    expect(errorWrapper.text()).toContain('Retry')
    errorWrapper.unmount()

    const endWrapper = mountFooter({ hasMore: false })
    expect(endWrapper.text()).toContain('End')
    endWrapper.unmount()
  })
})
