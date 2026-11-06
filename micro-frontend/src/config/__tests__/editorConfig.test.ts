import { describe, expect, it, vi } from 'vitest'
import { EditorState, type Extension } from '@codemirror/state'
import * as Y from 'yjs'

vi.mock('md-editor-v3', () => ({ config: vi.fn() }))

import {
  COLLABORATION_TICKET_REFRESH_INTERVAL_MS,
  collaborationPhaseAfterDisconnect,
  createCollaborationSession,
  createYjsBindingTransaction,
  isCollaborationEditable,
  shouldRefreshCollaborationTicket,
  yjsCompartment
} from '@/config/editorConfig'
import { createEditorYjsPersistenceKey } from '@/config/editorDraft'

describe('editorConfig', () => {
  it('replaces the local editor document when binding synchronized content', () => {
    const state = EditorState.create({
      doc: '1',
      extensions: [yjsCompartment.of([])]
    })
    const extension: Extension = []
    const transaction = state.update(createYjsBindingTransaction(1, 'remote content', extension))

    expect(transaction.state.doc.toString()).toBe('remote content')
  })

  it('refreshes missing or stale collaboration tickets', () => {
    const now = 1_000_000

    expect(shouldRefreshCollaborationTicket(0, COLLABORATION_TICKET_REFRESH_INTERVAL_MS, now)).toBe(
      true
    )
    expect(
      shouldRefreshCollaborationTicket(
        now - COLLABORATION_TICKET_REFRESH_INTERVAL_MS + 1,
        COLLABORATION_TICKET_REFRESH_INTERVAL_MS,
        now
      )
    ).toBe(false)
    expect(
      shouldRefreshCollaborationTicket(
        now - COLLABORATION_TICKET_REFRESH_INTERVAL_MS,
        COLLABORATION_TICKET_REFRESH_INTERVAL_MS,
        now
      )
    ).toBe(true)
  })

  it('keeps a previously synchronized editor editable while reconnecting', () => {
    expect(collaborationPhaseAfterDisconnect(false)).toBe('connecting')
    expect(isCollaborationEditable(false, false)).toBe(false)

    expect(collaborationPhaseAfterDisconnect(true)).toBe('reconnecting')
    expect(isCollaborationEditable(true, false)).toBe(true)
    expect(isCollaborationEditable(true, true)).toBe(false)
  })

  it('uses a user-isolated persistence key for every collaboration room', () => {
    expect(createEditorYjsPersistenceKey(7, '42', 'epoch')).toBe(
      'megalith:editor:yjs:v2:7:42:epoch'
    )
    expect(createEditorYjsPersistenceKey(7, undefined, 'epoch')).toBe(
      'megalith:editor:yjs:v2:7:new:epoch'
    )
    expect(createEditorYjsPersistenceKey(8, '42', 'epoch')).not.toBe(
      createEditorYjsPersistenceKey(7, '42', 'epoch')
    )
  })

  it('merges locally persisted and remote Yjs updates in one document model', () => {
    const persisted = new Y.Doc()
    const remote = new Y.Doc()
    persisted.getText().insert(0, 'local')
    remote.getText().insert(0, 'remote')

    Y.applyUpdate(persisted, Y.encodeStateAsUpdate(remote))
    Y.applyUpdate(remote, Y.encodeStateAsUpdate(persisted))

    expect(persisted.getText().toString()).toContain('local')
    expect(persisted.getText().toString()).toContain('remote')
    expect(remote.getText().toString()).toBe(persisted.getText().toString())

    persisted.destroy()
    remote.destroy()
  })

  it('falls back to online editing when IndexedDB is unavailable', async () => {
    const events: string[] = []
    const session = await createCollaborationSession(
      '42',
      'ticket',
      { id: 7, nickname: 'Chiu', avatar: '' },
      (event) => events.push(event.type),
      'megalith:editor:yjs:v2:7:42:epoch'
    )

    expect(events).toContain('persistence-error')
    session.destroy()
  })
})
