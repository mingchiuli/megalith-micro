import { flushPromises, mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import ReadTokenItem from '@/components/ReadTokenItem.vue'
import { protectedBlogStore } from '@/stores'
import type { BlogExhibit } from '@/type/entity'

const mocks = vi.hoisted(() => ({ POST: vi.fn(), push: vi.fn() }))

vi.mock('@/http/http', () => ({ useHttp: () => ({ POST: mocks.POST }) }))
vi.mock('vue-router', async (importOriginal) => ({
  ...(await importOriginal<typeof import('vue-router')>()),
  useRouter: () => ({ push: mocks.push })
}))

describe('ReadTokenItem', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('posts the token in JSON and routes without a query string', async () => {
    const blog: BlogExhibit = {
      title: 'Protected',
      description: 'd',
      content: 'c',
      avatar: '',
      readCount: 0,
      nickname: 'author',
      created: ''
    }
    mocks.POST.mockResolvedValue(blog)
    const wrapper = mount(ReadTokenItem, {
      attachTo: document.body,
      props: { blogId: 7, readTokenDialogVisible: true },
      global: { plugins: [ElementPlus] }
    })
    await flushPromises()
    const input = document.querySelector<HTMLInputElement>('.el-dialog input')!
    input.value = 'one-time-token'
    input.dispatchEvent(new Event('input', { bubbles: true }))
    document.querySelector<HTMLButtonElement>('.el-dialog__footer button')!.click()
    await flushPromises()

    expect(mocks.POST).toHaveBeenCalledWith('/public/blog/secret/7', {
      readToken: 'one-time-token'
    })
    expect(mocks.push).toHaveBeenCalledWith({ name: 'blog', params: { id: 7 } })
    expect(protectedBlogStore().take(7)).toEqual(blog)
    wrapper.unmount()
  })
})
