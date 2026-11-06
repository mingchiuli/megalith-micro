import { describe, expect, it, vi } from 'vitest'
import { defineComponent, h, createSSRApp } from 'vue'
import { renderToString } from '@vue/server-renderer'
import { flushPromises, mount } from '@vue/test-utils'
import { useEditorDraftIdentity } from '@/composables/useEditorDraftIdentity'

const editor = (
  create: () => Promise<{ id: number }>,
  navigate: (id: number) => Promise<unknown>,
  id?: string
) =>
  defineComponent({
    setup(_, { expose }) {
      expose(useEditorDraftIdentity(id, create, navigate))
      return () => h('div')
    }
  })

describe('editor draft identity', () => {
  it('never creates articles during SSR or while opening an existing article', async () => {
    const create = vi.fn(async () => ({ id: 42 }))
    const navigate = vi.fn(async (_id: number) => undefined)
    await renderToString(createSSRApp(editor(create, navigate)))
    const wrapper = mount(editor(create, navigate, '42'))
    await flushPromises()
    expect(create).not.toHaveBeenCalled()
    wrapper.unmount()
  })

  it('allocates separate identities for two new editors and suppresses repeated clicks', async () => {
    let next = 40
    const create = vi.fn(async () => ({ id: ++next }))
    const navigate = vi.fn(async (_id: number) => undefined)
    const first = mount(editor(create, navigate))
    const second = mount(editor(create, navigate))
    await (first.vm as unknown as { createDraft: () => Promise<void> }).createDraft()
    await flushPromises()
    expect(create).toHaveBeenCalledTimes(2)
    expect(navigate.mock.calls.map(([id]) => id)).toEqual([41, 42])
    first.unmount()
    second.unmount()
  })

  it('does not redirect a closed editor after a late creation response', async () => {
    let complete!: (value: { id: number }) => void
    const create = vi.fn(
      () =>
        new Promise<{ id: number }>((resolve) => {
          complete = resolve
        })
    )
    const navigate = vi.fn(async (_id: number) => undefined)
    const wrapper = mount(editor(create, navigate))
    wrapper.unmount()
    complete({ id: 42 })
    await flushPromises()
    expect(navigate).not.toHaveBeenCalled()
  })
})
