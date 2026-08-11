import { defineComponent, h } from 'vue'
import { mount } from '@vue/test-utils'
import IntroView from '@/views/IntroView.vue'

const RouterLinkStub = defineComponent({
  props: { to: { type: String, required: true } },
  setup:
    (_, { slots }) =>
    () =>
      h('a', slots.default?.())
})

describe('IntroView.vue', () => {
  it('keeps the two action icons in one aligned action row', () => {
    const wrapper = mount(IntroView, {
      global: {
        stubs: {
          RouterLink: RouterLinkStub,
          StatItem: true
        }
      }
    })

    const actionRow = wrapper.get('.into-button')
    const actions = actionRow.findAll('.intro-action-button')

    expect(actions).toHaveLength(2)
    expect(actions[0]!.find('.intro-notebook-icon').exists()).toBe(true)
    expect(actions[1]!.find('.intro-github-icon').exists()).toBe(true)
    expect(actionRow.findAll('.intro-action-button')).toHaveLength(2)
  })
})
