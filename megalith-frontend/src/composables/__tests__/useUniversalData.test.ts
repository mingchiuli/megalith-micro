import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { useUniversalData } from '@/composables/useUniversalData'
import { ssrDataStore } from '@/stores'

describe('useUniversalData', () => {
  beforeEach(() => setActivePinia(createPinia()))

  it('consumes hydrated data without issuing another browser request', () => {
    ssrDataStore().set('page', { title: 'hydrated' })
    const loader = vi.fn()
    const apply = vi.fn()
    const component = defineComponent({
      setup() {
        useUniversalData('page', loader, apply)
        return () => null
      }
    })

    const wrapper = mount(component)

    expect(apply).toHaveBeenCalledWith({ title: 'hydrated' })
    expect(loader).not.toHaveBeenCalled()
    expect(ssrDataStore().has('page')).toBe(false)
    wrapper.unmount()
  })

  it('loads on mount and refreshes without retaining SPA cache data', async () => {
    const loader = vi
      .fn()
      .mockResolvedValueOnce({ title: 'first' })
      .mockResolvedValueOnce({ title: 'second' })
    const apply = vi.fn()
    let refresh!: () => Promise<{ title: string }>
    let invalidate!: () => void
    const component = defineComponent({
      setup() {
        const universalData = useUniversalData('page', loader, apply)
        refresh = universalData.refresh
        invalidate = universalData.invalidate
        return () => null
      }
    })
    const wrapper = mount(component)
    await flushPromises()

    expect(apply).toHaveBeenLastCalledWith({ title: 'first' })
    await refresh()
    ssrDataStore().set('page', { title: 'temporary' })
    invalidate()
    expect(apply).toHaveBeenLastCalledWith({ title: 'second' })
    expect(ssrDataStore().has('page')).toBe(false)
    wrapper.unmount()
  })
})
