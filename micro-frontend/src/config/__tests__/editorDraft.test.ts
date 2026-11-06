import { describe, expect, it, vi } from 'vitest'
import {
  createEditorDraftId,
  createEditorMetadataDraftKey,
  createEditorYjsPersistenceKey,
  toEditorMetadataDraft
} from '@/config/editorDraft'
import { Status, SensitiveType, type EditForm } from '@/type/entity'

describe('editorDraft', () => {
  it('isolates new and existing drafts by user and blog', () => {
    expect(createEditorDraftId(7, undefined, 'epoch')).toBe('7:new:epoch')
    expect(createEditorDraftId(7, '42', 'epoch')).toBe('7:42:epoch')
    expect(createEditorMetadataDraftKey(7, '42', 'epoch')).toBe(
      'megalith:editor:metadata:v2:7:42:epoch'
    )
    expect(createEditorYjsPersistenceKey(7, '42', 'epoch')).toBe(
      'megalith:editor:yjs:v2:7:42:epoch'
    )
    expect(createEditorYjsPersistenceKey(8, '42', 'epoch')).not.toBe(
      createEditorYjsPersistenceKey(7, '42', 'epoch')
    )
  })

  it('serializes only editable metadata and clones sensitive ranges', () => {
    vi.setSystemTime(new Date('2026-08-26T00:00:00.000Z'))
    const form: Pick<
      EditForm,
      'title' | 'description' | 'status' | 'link' | 'sensitiveContentList'
    > = {
      title: 'Draft title',
      description: 'Draft description',
      status: Status.SENSITIVE_FILTER,
      link: 'cover.webp',
      sensitiveContentList: [{ startIndex: 1, endIndex: 3, type: SensitiveType.CONTENT }]
    }

    const draft = toEditorMetadataDraft('draft-key', form)

    expect(draft).toEqual({
      key: 'draft-key',
      title: 'Draft title',
      description: 'Draft description',
      status: Status.SENSITIVE_FILTER,
      link: 'cover.webp',
      sensitiveContentList: [{ startIndex: 1, endIndex: 3, type: SensitiveType.CONTENT }],
      updatedAt: new Date('2026-08-26T00:00:00.000Z').getTime()
    })
    expect(draft.sensitiveContentList).not.toBe(form.sensitiveContentList)
    vi.useRealTimers()
  })
})
