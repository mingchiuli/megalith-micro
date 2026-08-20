import { beforeEach, describe, expect, it, vi } from 'vitest'
import { defineComponent, h } from 'vue'
import { flushPromises, mount, shallowMount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createMemoryHistory, createRouter } from 'vue-router'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { loginStateStore, menuStore, tabStore } from '@/stores'
import { RoutesEnum, RoutesStatus, type Menu } from '@/type/entity'
import BackHeaderItem from '@/components/sys/BackHeaderItem.vue'
import HeaderTabsItem from '@/components/sys/HeaderTabsItem.vue'
import InfiniteMenuItem from '@/components/sys/InfiniteMenuItem.vue'
import SideMenuItem from '@/components/sys/SideMenuItem.vue'
import SystemView from '@/views/sys/SystemView.vue'

vi.mock('@/utils/auth', () => ({
  useAuth: () => ({ logout: vi.fn() })
}))

const menuNode = (overrides: Partial<Menu> = {}): Menu => ({
  id: 1,
  parentId: 0,
  title: 'Content',
  name: 'content',
  icon: 'Setting',
  orderNum: 0,
  status: RoutesStatus.NORMAL,
  type: RoutesEnum.MENU,
  url: '/backend/content',
  component: 'sys/ContentView',
  children: [],
  ...overrides
})

const createBackendRouter = () =>
  createRouter({
    history: createMemoryHistory(),
    routes: [
      {
        path: '/backend',
        name: 'backend-root',
        component: { template: '<div />' },
        children: [
          {
            path: 'content',
            name: 'content',
            component: { template: '<div />' }
          }
        ]
      }
    ]
  })

const ElTabsStub = defineComponent({
  name: 'ElTabs',
  props: {
    modelValue: { type: String, default: '' }
  },
  emits: ['update:modelValue', 'tab-remove', 'tab-click'],
  setup:
    (_, { slots }) =>
    () =>
      h('div', slots.default?.())
})

const ElTabPaneStub = defineComponent({
  name: 'ElTabPane',
  props: {
    label: { type: String, default: '' },
    name: { type: String, default: '' }
  },
  setup: () => () => h('div')
})

const SlotStub = defineComponent({
  setup:
    (_, { slots }) =>
    () =>
      h('div', slots.default?.())
})

const ElTextStub = defineComponent({
  name: 'ElText',
  setup:
    (_, { slots }) =>
    () =>
      h('span', slots.default?.())
})

const ElDropdownStub = defineComponent({
  name: 'ElDropdown',
  props: {
    trigger: {
      type: [String, Array],
      default: 'hover'
    },
    teleported: { type: Boolean, default: true }
  },
  setup:
    (_, { slots }) =>
    () =>
      h('div', [slots.default?.(), slots.dropdown?.()])
})

describe('backend navigation', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
    setActivePinia(createPinia())
  })

  it('returns to the dynamic backend root after closing the last tab', async () => {
    const pinia = createPinia()
    setActivePinia(pinia)
    const router = createBackendRouter()
    await router.push({ name: 'content' })
    await router.isReady()

    menuStore().menuTree = menuNode({
      name: 'backend-root',
      title: 'Backend',
      type: RoutesEnum.CATALOGUE,
      url: '/backend'
    })
    tabStore().addTab({ name: 'content', title: 'Content' })

    const wrapper = mount(HeaderTabsItem, {
      global: {
        plugins: [pinia, router],
        stubs: { ElTabs: ElTabsStub, ElTabPane: ElTabPaneStub }
      }
    })

    wrapper.getComponent(ElTabsStub).vm.$emit('tab-remove', 'content')
    await flushPromises()

    expect(tabStore().editableTabs).toEqual([])
    expect(tabStore().editableTabsValue).toBe('')
    expect(router.currentRoute.value.name).toBe('backend-root')
  })

  it('renders either the backend welcome or routed content', async () => {
    const pinia = createPinia()
    setActivePinia(pinia)
    const router = createBackendRouter()
    menuStore().menuTree = menuNode({
      name: 'backend-root',
      title: 'Backend',
      type: RoutesEnum.CATALOGUE,
      url: '/backend'
    })
    await router.push({ name: 'backend-root' })
    await router.isReady()

    const wrapper = mount(SystemView, {
      global: {
        plugins: [pinia, router],
        stubs: {
          ElContainer: SlotStub,
          ElAside: SlotStub,
          ElScrollbar: SlotStub,
          ElHeader: SlotStub,
          ElMain: SlotStub,
          ElText: SlotStub,
          ElFooter: SlotStub,
          SideMenuItem: true,
          BackHeaderItem: true,
          HeaderTabsItem: true,
          MyFooterItem: true,
          RouterView: true
        }
      }
    })

    expect(wrapper.find('.welcome').exists()).toBe(true)
    expect(wrapper.find('.content').exists()).toBe(false)

    await router.push({ name: 'content' })
    await flushPromises()

    expect(wrapper.find('.welcome').exists()).toBe(false)
    expect(wrapper.find('.content').exists()).toBe(true)
  })

  it('keeps the user dropdown in the SSR tree and supports click as well as hover', async () => {
    const pinia = createPinia()
    setActivePinia(pinia)
    const router = createBackendRouter()
    await router.push({ name: 'backend-root' })
    loginStateStore().user = { id: 1, nickname: 'Chiu', avatar: '' }

    const wrapper = shallowMount(BackHeaderItem, {
      global: {
        plugins: [pinia, router],
        stubs: {
          ElText: ElTextStub,
          ElDropdown: ElDropdownStub
        }
      }
    })

    expect(wrapper.get('.header-content').element.tagName).toBe('DIV')
    expect(wrapper.get('.header-title').element.tagName).toBe('SPAN')
    expect(wrapper.get('.header-title').findComponent(ElDropdownStub).exists()).toBe(false)
    expect(wrapper.get('.header-actions').findComponent(ElDropdownStub).exists()).toBe(true)
    expect(wrapper.getComponent(ElDropdownStub).props('trigger')).toEqual(['click', 'hover'])
    expect(wrapper.getComponent(ElDropdownStub).props('teleported')).toBe(false)
  })

  it('keeps the native submenu title structure for collapsed menu styling', () => {
    const router = createBackendRouter()
    const catalogue = menuNode({
      type: RoutesEnum.CATALOGUE,
      name: 'catalogue',
      title: 'Catalogue'
    })
    const ElSubMenuStub = defineComponent({
      name: 'ElSubMenu',
      props: { index: { type: String, required: true } },
      setup:
        (_, { slots }) =>
        () =>
          h('div', [h('div', { class: 'submenu-title' }, slots.title?.()), slots.default?.()])
    })

    const wrapper = mount(InfiniteMenuItem, {
      props: { item: catalogue },
      global: {
        plugins: [router],
        stubs: { ElSubMenu: ElSubMenuStub }
      }
    })

    const title = wrapper.get('.submenu-title')
    expect(Array.from(title.element.children).map((element) => element.tagName)).toEqual([
      'I',
      'SPAN'
    ])
    expect(title.text()).toBe('Catalogue')
  })

  it('keeps the arrow aligned with the actual side-menu state', async () => {
    const pinia = createPinia()
    setActivePinia(pinia)
    vi.spyOn(document.body, 'clientWidth', 'get').mockReturnValue(600)

    const ElButtonStub = defineComponent({
      name: 'ElButton',
      props: { icon: { default: undefined } },
      emits: ['click'],
      setup:
        (_, { emit }) =>
        () =>
          h('button', { onClick: () => emit('click') })
    })
    const ElMenuStub = defineComponent({
      name: 'ElMenu',
      props: { collapse: Boolean },
      setup: () => () => h('div')
    })

    const wrapper = mount(SideMenuItem, {
      global: {
        plugins: [pinia],
        stubs: {
          ElButton: ElButtonStub,
          ElMenu: ElMenuStub,
          InfiniteMenuItem: true
        }
      }
    })
    await flushPromises()

    const button = wrapper.getComponent(ElButtonStub)
    const menu = wrapper.getComponent(ElMenuStub)
    expect(menu.props('collapse')).toBe(true)
    expect(button.props('icon')).toBe(ArrowRight)

    await button.trigger('click')
    expect(menu.props('collapse')).toBe(false)
    expect(button.props('icon')).toBe(ArrowLeft)

    window.dispatchEvent(new Event('resize'))
    await flushPromises()
    expect(menu.props('collapse')).toBe(false)
    expect(button.props('icon')).toBe(ArrowLeft)
  })
})
