import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { defineComponent, h } from 'vue'
import { displayState } from '@/utils/position'

// 通过宿主组件让 onMounted / onUnmounted 生效
const Host = defineComponent({
  setup() {
    const state = displayState()
    return { state }
  },
  render() {
    return h('div')
  }
})

const setWidth = (width: number) => {
  Object.defineProperty(document.body, 'clientWidth', {
    configurable: true,
    get: () => width
  })
}

describe('utils/position#displayState', () => {
  let addSpy: ReturnType<typeof vi.spyOn>
  let removeSpy: ReturnType<typeof vi.spyOn>

  beforeEach(() => {
    addSpy = vi.spyOn(window, 'addEventListener')
    removeSpy = vi.spyOn(window, 'removeEventListener')
  })

  afterEach(() => {
    addSpy.mockRestore()
    removeSpy.mockRestore()
  })

  it('宽度大于 900 时启用展开布局', () => {
    setWidth(1280)
    const wrapper = mount(Host)
    const { state } = wrapper.vm as unknown as {
      state: ReturnType<typeof displayState>
    }
    expect(state.expand.value).toBe(true)
    expect(state.fix.value).toBe('right')
    expect(state.fixSelection.value).toBe('left')
    expect(state.moreItems.value).toBe(false)
    wrapper.unmount()
  })

  it('宽度小于 900 时收起', () => {
    setWidth(600)
    const wrapper = mount(Host)
    const { state } = wrapper.vm as unknown as {
      state: ReturnType<typeof displayState>
    }
    expect(state.expand.value).toBe(false)
    expect(state.fix.value).toBe(false)
    expect(state.fixSelection.value).toBe(false)
    wrapper.unmount()
  })

  it('宽度大于 2000 时打开 moreItems', () => {
    setWidth(2200)
    const wrapper = mount(Host)
    const { state } = wrapper.vm as unknown as {
      state: ReturnType<typeof displayState>
    }
    expect(state.moreItems.value).toBe(true)
    wrapper.unmount()
  })

  it('mount 时注册 resize 监听，unmount 时移除', () => {
    setWidth(1024)
    const wrapper = mount(Host)
    expect(addSpy).toHaveBeenCalledWith('resize', expect.any(Function))
    wrapper.unmount()
    expect(removeSpy).toHaveBeenCalledWith('resize', expect.any(Function))
  })

  it('resize 时根据当前宽度更新响应式状态', () => {
    setWidth(1024)
    const wrapper = mount(Host)
    const { state } = wrapper.vm as unknown as {
      state: ReturnType<typeof displayState>
    }
    expect(state.expand.value).toBe(true)

    // 找到本次 mount 注册的 resize 处理函数并手动触发
    const handler = addSpy.mock.calls.find((c) => c[0] === 'resize')?.[1] as
      | EventListener
      | undefined
    expect(handler).toBeTypeOf('function')

    setWidth(600)
    handler!(new Event('resize'))
    expect(state.expand.value).toBe(false)
    expect(state.fix.value).toBe(false)

    wrapper.unmount()
  })
})
