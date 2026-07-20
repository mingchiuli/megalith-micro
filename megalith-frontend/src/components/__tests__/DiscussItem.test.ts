import { afterEach, describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import * as ElIcons from '@element-plus/icons-vue'
import DiscussItem from '@/components/DiscussItem.vue'
import { themeStore } from '@/stores'

// 统一的 Element Plus 挂载配置：注册组件库 + 全部图标
const buildMountOptions = (pinia = createPinia()) => ({
  attachTo: document.body,
  global: {
    plugins: [pinia, ElementPlus],
    components: { ...ElIcons }
  }
})

describe('DiscussItem.vue', () => {
  afterEach(() => {
    document.body.innerHTML = ''
  })

  it('渲染 Disqus / Giscus 两个 tab', async () => {
    const wrapper = mount(DiscussItem, buildMountOptions())
    await nextTick()

    expect(wrapper.find('#disqus_thread').exists()).toBe(true)
    expect(wrapper.find('#giscus_thread').exists()).toBe(true)
    expect(wrapper.text()).toContain('Disqus')
    expect(wrapper.text()).toContain('Giscus')
  })

  it('在 onMounted 中注入 disqus 与 giscus 脚本', async () => {
    const wrapper = mount(DiscussItem, buildMountOptions())
    await nextTick()

    const disqusScript = document
      .getElementById('disqus_thread')!
      .querySelector('script')
    const giscusScript = document
      .getElementById('giscus_thread')!
      .querySelector('script')

    expect(disqusScript?.src).toContain('disqus.com/embed.js')
    expect(giscusScript?.src).toContain('giscus.app/client.js')
    expect(giscusScript?.getAttribute('data-repo')).toBe(
      'mingchiuli/megalith-talk-repo'
    )
    expect(giscusScript?.getAttribute('data-theme')).toBe('light')

    wrapper.unmount()
  })

  it('使用当前暗色主题加载 giscus', async () => {
    const pinia = createPinia()
    themeStore(pinia).isDark = true
    const wrapper = mount(DiscussItem, buildMountOptions(pinia))
    await nextTick()

    const giscusScript = document
      .getElementById('giscus_thread')!
      .querySelector('script')

    expect(giscusScript?.getAttribute('data-theme')).toBe('dark')

    wrapper.unmount()
  })
})
