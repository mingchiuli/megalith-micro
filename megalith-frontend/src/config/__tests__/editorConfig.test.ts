import { describe, expect, it, vi } from 'vitest'
import { EditorState, type Extension } from '@codemirror/state'
import * as Y from 'yjs'

vi.mock('md-editor-v3', () => ({ config: vi.fn() }))

import {
  COLLABORATION_TICKET_REFRESH_INTERVAL_MS,
  createYjsBindingTransaction,
  hasYjsDocumentState,
  shouldRefreshCollaborationTicket,
  shouldInitializeYjsDocument,
  yjsCompartment
} from '@/config/editorConfig'

describe('editorConfig', () => {
  it('recognizes a recreated empty Y.Doc as state-less', () => {
    const doc = new Y.Doc()
    const text = doc.getText()

    expect(hasYjsDocumentState(doc)).toBe(false)
    expect(shouldInitializeYjsDocument(doc, text, 'database content')).toBe(true)
  })

  it('distinguishes an intentionally cleared document from a new empty document', () => {
    const doc = new Y.Doc()
    const text = doc.getText()
    text.insert(0, 'remote content')
    text.delete(0, text.length)

    expect(text.length).toBe(0)
    expect(hasYjsDocumentState(doc)).toBe(true)
    expect(shouldInitializeYjsDocument(doc, text, 'database content')).toBe(false)
  })

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
})
