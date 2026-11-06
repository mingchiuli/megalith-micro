import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { COLLABORATION_CONNECTION_NOTICE_DELAY_MS } from '@/config/editorConfig'
import type { CollaborationEvent } from '@/config/editorConfig'

const mocks = vi.hoisted(() => ({
  notify: vi.fn(),
  POST: vi.fn(),
  createCollaborationSession: vi.fn()
}))

vi.mock('element-plus', async (importOriginal) => {
  const actual = await importOriginal<typeof import('element-plus')>()
  return { ...actual, ElNotification: mocks.notify }
})

vi.mock('@/http/http', () => ({
  useHttp: () => ({ POST: mocks.POST, UPLOAD: vi.fn() })
}))

vi.mock('vue-router', async (importOriginal) => ({
  ...(await importOriginal<typeof import('vue-router')>()),
  useRoute: () => ({ query: { id: '42' } })
}))

vi.mock('md-editor-v3', async () => {
  const { defineComponent, h } = await import('vue')
  return {
    config: vi.fn(),
    MdEditor: defineComponent({
      name: 'MdEditor',
      setup(_, { expose, slots }) {
        expose({
          getEditorView: () => ({ state: { doc: { length: 0 } }, dispatch: vi.fn() })
        })
        return () => h('div', slots.default?.())
      }
    })
  }
})

vi.mock('@vavt/v3-extension', () => ({ ExportPDF: {}, Emoji: {} }))

vi.mock('@/config/editorConfig', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/config/editorConfig')>()
  return { ...actual, createCollaborationSession: mocks.createCollaborationSession }
})

import EditorItem from '@/components/sys/EditorItem.vue'

const sessionStub = {
  config: [],
  persistenceKey: 'megalith:editor:yjs:v1:0:42',
  connect: vi.fn(),
  disconnect: vi.fn(),
  updateToken: vi.fn(),
  restart: vi.fn(),
  clearPersistence: vi.fn(),
  destroy: vi.fn()
}

const mountEditor = async () => {
  const wrapper = mount(EditorItem, {
    props: { blogId: 42, collaborationEpoch: 'epoch', formStatus: 0, manageAssets: false },
    global: { plugins: [createPinia()] }
  })
  await flushPromises()
  return wrapper
}

const noticeTypes = () => mocks.notify.mock.calls.map(([options]) => options.type)

describe('collaborative editor connection notices', () => {
  let emitCollaborationEvent: (event: CollaborationEvent) => void

  beforeEach(() => {
    setActivePinia(createPinia())
    mocks.notify.mockReset()
    mocks.POST.mockReset()
    mocks.createCollaborationSession.mockReset()
    mocks.POST.mockImplementation(async () => ({
      ticket: 'ticket',
      roomId: 'blog:42:epoch',
      collaborationEpoch: 'epoch',
      expiresAt: Date.now() + 60_000
    }))
    mocks.createCollaborationSession.mockImplementation(async (_roomId, _token, _user, onEvent) => {
      emitCollaborationEvent = onEvent
      return sessionStub
    })
  })

  afterEach(() => {
    vi.useRealTimers()
    vi.clearAllMocks()
  })

  it('keeps a reconnect that heals itself silent instead of warning about the drop', async () => {
    const wrapper = await mountEditor()
    emitCollaborationEvent({ type: 'connected' })
    emitCollaborationEvent({ type: 'synced', content: 'content' })
    expect(noticeTypes()).toEqual(['success'])

    mocks.notify.mockClear()
    vi.useFakeTimers()
    emitCollaborationEvent({ type: 'disconnected' })
    emitCollaborationEvent({ type: 'connected' })
    emitCollaborationEvent({ type: 'synced', content: 'content' })
    vi.advanceTimersByTime(COLLABORATION_CONNECTION_NOTICE_DELAY_MS * 2)

    expect(noticeTypes()).toEqual([])
    wrapper.unmount()
  })

  it('warns once the collaboration stays unsynchronized and confirms the recovery', async () => {
    const wrapper = await mountEditor()
    emitCollaborationEvent({ type: 'connected' })
    emitCollaborationEvent({ type: 'synced', content: 'content' })

    mocks.notify.mockClear()
    vi.useFakeTimers()
    emitCollaborationEvent({ type: 'connection-closed', code: 1013, reason: 'unavailable' })
    expect(noticeTypes()).toEqual([])

    vi.advanceTimersByTime(COLLABORATION_CONNECTION_NOTICE_DELAY_MS)
    expect(noticeTypes()).toEqual(['warning'])

    emitCollaborationEvent({ type: 'synced', content: 'content' })
    expect(noticeTypes()).toEqual(['warning', 'success'])
    wrapper.unmount()
  })
  it('renews and reconnects before authorization expires without a warning', async () => {
    vi.useFakeTimers()
    const wrapper = await mountEditor()
    emitCollaborationEvent({ type: 'synced', content: 'content' })
    mocks.notify.mockClear()
    await vi.advanceTimersByTimeAsync(45_000)
    await flushPromises()
    expect(mocks.POST).toHaveBeenCalledTimes(2)
    expect(sessionStub.restart).toHaveBeenCalledWith('ticket')
    expect(noticeTypes()).toEqual([])
    wrapper.unmount()
  })

  it('retains old content and asks the parent to reload on an epoch change', async () => {
    vi.useFakeTimers()
    const wrapper = await mountEditor()
    emitCollaborationEvent({ type: 'synced', content: 'unsaved old content' })
    mocks.POST.mockResolvedValue({
      ticket: 'new',
      roomId: 'blog:42:new',
      collaborationEpoch: 'new',
      expiresAt: Date.now() + 60_000
    })
    await vi.advanceTimersByTimeAsync(45_000)
    await flushPromises()
    expect(wrapper.emitted('epochChanged')).toHaveLength(1)
    expect(wrapper.emitted('authorizationPaused')).toHaveLength(1)
    expect(sessionStub.disconnect).toHaveBeenCalled()
    expect(sessionStub.restart).not.toHaveBeenCalledWith('new')
    wrapper.unmount()
  })

  it('pauses instead of reconnecting indefinitely after permission is denied', async () => {
    vi.useFakeTimers()
    const wrapper = await mountEditor()
    emitCollaborationEvent({ type: 'synced', content: 'content' })
    mocks.POST.mockRejectedValue({ response: { status: 403 } })
    await vi.advanceTimersByTimeAsync(45_000)
    await flushPromises()
    expect(wrapper.emitted('authorizationPaused')).toHaveLength(1)
    expect(sessionStub.disconnect).toHaveBeenCalled()
    const requests = mocks.POST.mock.calls.length
    await vi.advanceTimersByTimeAsync(30_000)
    expect(mocks.POST).toHaveBeenCalledTimes(requests)
    wrapper.unmount()
  })
})
