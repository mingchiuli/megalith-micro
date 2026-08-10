import { beforeEach, describe, expect, it, vi } from 'vitest'
import { defineComponent, h } from 'vue'
import { flushPromises, mount, shallowMount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createMemoryHistory, createRouter } from 'vue-router'
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

const ElDropdownStub = defineComponent({
  name: 'ElDropdown',
  props: {
    trigger: {
      type: [String, Array],
      default: 'hover'
    }
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

  it('opens the user dropdown by click as well as hover', async () => {
    const pinia = createPinia()
    setActivePinia(pinia)
    const router = createBackendRouter()
    await router.push({ name: 'backend-root' })
    loginStateStore().user = { id: 1, nickname: 'Chiu', avatar: '' }

    const wrapper = shallowMount(BackHeaderItem, {
      global: {
        plugins: [pinia, router],
        stubs: {
          ElText: SlotStub,
          ElDropdown: ElDropdownStub
        }
      }
    })

    expect(wrapper.getComponent(ElDropdownStub).props('trigger')).toEqual(['click', 'hover'])
  })

  it('emits an explicit catalogue toggle only while the side menu is collapsed', async () => {
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
          h('div', [slots.title?.(), slots.default?.()])
    })

    const wrapper = mount(InfiniteMenuItem, {
      props: { item: catalogue, collapsed: true },
      global: {
        plugins: [router],
        stubs: { ElSubMenu: ElSubMenuStub }
      }
    })

    await wrapper.get('.catalogue-title').trigger('click')
    expect(wrapper.emitted('openCatalogue')).toEqual([[String(catalogue.id)]])

    await wrapper.setProps({ collapsed: false })
    await wrapper.get('.catalogue-title').trigger('click')
    expect(wrapper.emitted('openCatalogue')).toHaveLength(1)
  })

  it('toggles the right-side catalogue popup from the collapsed menu', async () => {
    const pinia = createPinia()
    setActivePinia(pinia)
    const router = createBackendRouter()
    await router.push({ name: 'backend-root' })
    menuStore().menuTree = menuNode({
      name: 'backend-root',
      type: RoutesEnum.CATALOGUE,
      url: '/backend',
      children: [menuNode({ type: RoutesEnum.CATALOGUE, name: 'catalogue' })]
    })
    vi.spyOn(document.body, 'clientWidth', 'get').mockReturnValue(600)

    const open = vi.fn()
    const close = vi.fn()
    const ElMenuStub = defineComponent({
      name: 'ElMenu',
      props: {
        collapse: Boolean,
        closeOnClickOutside: Boolean,
        defaultActive: { type: String, default: '' }
      },
      emits: ['open', 'close', 'select'],
      setup: (_, { emit, expose, slots }) => {
        const openMenu = (index: string) => {
          open(index)
          emit('open', index, [])
        }
        const closeMenu = (index: string) => {
          close(index)
          emit('close', index, [])
        }
        expose({ open: openMenu, close: closeMenu })
        return () => h('div', slots.default?.())
      }
    })
    const InfiniteMenuItemStub = defineComponent({
      name: 'InfiniteMenuItem',
      props: {
        item: { type: Object, required: true },
        collapsed: Boolean
      },
      emits: ['open-catalogue'],
      setup:
        (props, { emit }) =>
        () =>
          h('button', {
            class: 'catalogue-trigger',
            onClick: () => emit('open-catalogue', String((props.item as Menu).id))
          })
    })

    const wrapper = mount(SideMenuItem, {
      global: {
        plugins: [pinia, router],
        stubs: {
          ElButton: true,
          ElMenu: ElMenuStub,
          InfiniteMenuItem: InfiniteMenuItemStub
        }
      }
    })
    await flushPromises()

    const menu = wrapper.getComponent(ElMenuStub)
    expect(menu.props('collapse')).toBe(true)
    expect(menu.props('closeOnClickOutside')).toBe(true)

    await wrapper.get('.catalogue-trigger').trigger('click')
    expect(open).toHaveBeenCalledWith('1')

    await wrapper.get('.catalogue-trigger').trigger('click')
    expect(close).toHaveBeenCalledWith('1')
  })
})
