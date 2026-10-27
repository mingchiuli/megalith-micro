import { afterEach, describe, expect, it, vi } from 'vitest'
import { defineComponent, h } from 'vue'
import { enableAutoUnmount, flushPromises, mount } from '@vue/test-utils'
import { createPinia } from 'pinia'
import ElementPlus, { ElFormItem, ElInput, ElOption, ElSelect } from 'element-plus'
import { ButtonAuth, type AuthoritySys } from '@/type/entity'

const mocks = vi.hoisted(() => ({
  GET: vi.fn(),
  POST: vi.fn(),
  DOWNLOAD: vi.fn()
}))

vi.mock('@/http/http', () => ({
  useHttp: () => mocks
}))

vi.mock('@/utils/permissions', () => ({
  checkButtonAuth: () => true,
  getButtonTitle: (name: string) => name,
  getButtonType: () => ''
}))

import AuthorityView from '@/views/sys/AuthorityView.vue'

enableAutoUnmount(afterEach)

const DialogStub = defineComponent({
  props: {
    modelValue: Boolean
  },
  setup(props, { slots }) {
    return () => (props.modelValue ? h('div', slots.default?.()) : null)
  }
})

describe('authority editor', () => {
  afterEach(() => vi.clearAllMocks())

  it('submits the selected protocol from the real parent form', async () => {
    mocks.GET.mockResolvedValue([] satisfies AuthoritySys[])
    mocks.POST.mockResolvedValue(null)

    const wrapper = mount(AuthorityView, {
      global: {
        plugins: [createPinia(), ElementPlus],
        stubs: { ElDialog: DialogStub }
      }
    })
    await flushPromises()

    const create = wrapper
      .findAll('button')
      .find((button) => button.text() === ButtonAuth.SYS_AUTHORITY_CREATE)!
    await create.trigger('click')

    const field = (prop: string) =>
      wrapper.findAllComponents(ElFormItem).find((item) => item.props('prop') === prop)!
    const fillInput = async (prop: string, value: string) => {
      await field(prop).getComponent(ElInput).get('input').setValue(value)
    }
    const selectOption = async (prop: string, value: string) => {
      const select = field(prop).getComponent(ElSelect)
      await select.get('.el-select__wrapper').trigger('click')
      const option = select
        .findAllComponents(ElOption)
        .find((item) => item.props('value') === value)!
      await option.trigger('click')
      await flushPromises()
    }

    await fillInput('code', 'blog-delete')
    await fillInput('remark', 'Delete blog')
    await selectOption('protocol', 'http')
    await selectOption('methodType', 'POST')
    await fillInput('routePattern', '/sys/blog/deleted')
    await selectOption('serviceHost', 'micro-user')

    const save = wrapper
      .findAll('button')
      .find((button) => button.text() === ButtonAuth.SYS_AUTHORITY_SAVE)!
    await save.trigger('click')
    await flushPromises()

    expect(mocks.POST).toHaveBeenCalledWith('/sys/authority/save', expect.any(Object))
    const [, payload] = mocks.POST.mock.calls[0]!
    const requestBody = JSON.parse(JSON.stringify(payload))
    expect(requestBody).toEqual({
      code: 'blog-delete',
      remark: 'Delete blog',
      protocol: 'http',
      methodType: 'POST',
      routePattern: '/sys/blog/deleted',
      serviceHost: 'micro-user',
      servicePort: 8086,
      status: 0,
      type: 0
    })
    expect(requestBody).not.toHaveProperty('prototype')
  })
})
