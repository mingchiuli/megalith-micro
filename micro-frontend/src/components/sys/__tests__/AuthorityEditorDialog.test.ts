import { afterEach, describe, expect, it, vi } from 'vitest'
import { defineComponent, h, nextTick, reactive } from 'vue'
import { enableAutoUnmount, flushPromises, mount } from '@vue/test-utils'
import { ElFormItem, ElInput, ElOption, ElSelect } from 'element-plus'
import { AuthStatus, Status } from '@/type/entity'
import AuthorityEditorDialog from '@/components/sys/AuthorityEditorDialog.vue'

vi.mock('@/utils/permissions', () => ({
  checkButtonAuth: () => false,
  getButtonTitle: () => 'Save',
  getButtonType: () => 'primary'
}))

enableAutoUnmount(afterEach)

const mountDialog = (serviceHost = '', servicePort = 8081) => {
  const form = reactive({
    id: 1,
    code: 'blog-read',
    remark: 'Read blog',
    prototype: 'http',
    methodType: 'GET',
    routePattern: '/public/blog/info/*',
    serviceHost,
    servicePort,
    status: Status.NORMAL,
    type: AuthStatus.NEED_AUTH
  })
  const props = reactive({ visible: true, form, rules: {} })
  const Host = defineComponent({
    setup: () => () => h(AuthorityEditorDialog, props)
  })
  const wrapper = mount(Host, {
    global: {
      stubs: { ElDialog: { template: '<div><slot /></div>' } }
    }
  })
  const field = (prop: string) =>
    wrapper.findAllComponents(ElFormItem).find((item) => item.props('prop') === prop)!
  const setVisible = async (visible: boolean) => {
    props.visible = visible
    await nextTick()
  }
  const selectService = async (host: string) => {
    const select = field('serviceHost').getComponent(ElSelect)
    await select.get('.el-select__wrapper').trigger('click')
    const option = select.findAllComponents(ElOption).find((item) => item.props('value') === host)!
    await option.trigger('click')
    await flushPromises()
  }

  return { form, field, setVisible, selectService }
}

describe('authority service selection', () => {
  it.each([
    ['micro-blog', 8082],
    ['micro-user', 8086],
    ['micro-auth', 8081],
    ['micro-search', 8085],
    ['micro-exhibit', 8083],
    ['micro-sync-rs', 8089]
  ])('fills the numeric default port for %s', async (host, port) => {
    const { form, field, selectService } = mountDialog()

    await selectService(host)

    expect(form.serviceHost).toBe(host)
    expect(form.servicePort).toBe(port)
    expect(field('servicePort').getComponent(ElInput).props('modelValue')).toBe(port)
    expect(form.prototype).toBe('http')
    expect(form.methodType).toBe('GET')
    expect(form.routePattern).toBe('/public/blog/info/*')
  })

  it.each(['micro-blog', 'custom-blog-host'])(
    'preserves an existing custom port for %s when opening the dialog',
    async (host) => {
      const { form, setVisible } = mountDialog(host, 9100)
      await flushPromises()

      expect(form.serviceHost).toBe(host)
      expect(form.servicePort).toBe(9100)

      await setVisible(false)
      Object.assign(form, { serviceHost: 'micro-search', servicePort: 9200 })
      await setVisible(true)
      await flushPromises()

      expect(form.serviceHost).toBe('micro-search')
      expect(form.servicePort).toBe(9200)
    }
  )

  it('preserves manual edits until the user selects another service', async () => {
    const { form, field, setVisible, selectService } = mountDialog('micro-blog', 9100)
    await selectService('micro-user')
    expect(form.servicePort).toBe(8086)

    const port = field('servicePort').getComponent(ElInput).get('input')
    await port.setValue('9300')
    form.remark = 'Updated remark'
    await nextTick()
    await setVisible(false)
    await setVisible(true)
    expect(port.element.value).toBe('9300')

    await selectService('micro-sync-rs')
    expect(form.servicePort).toBe(8089)
    expect(port.element.value).toBe('8089')
  })
})
