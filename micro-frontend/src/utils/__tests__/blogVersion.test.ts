import { describe, expect, it, vi } from 'vitest'
import { saveBlogVersion } from '@/utils/blogVersion'
import type { EditForm } from '@/type/entity'
import { Status } from '@/type/entity'

describe('saveBlogVersion', () => {
  it('persists one version request and returns the updated draft revision', async () => {
    const version = {
      id: 9,
      blogId: 7,
      versionNo: 2,
      origin: 'DRAFT' as const,
      title: 'title',
      firstPublishedAt: undefined,
      createdBy: 1,
      createdAt: '2026-11-01 10:00:00',
      description: 'description',
      content: 'content',
      link: '',
      renderMode: 'NORMAL' as const,
      sensitiveContent: []
    }
    const post = vi.fn().mockResolvedValue({ draftRevision: 3, version })
    const form: EditForm = {
      id: 7,
      userId: 1,
      title: 'title',
      description: 'description',
      content: 'content',
      status: Status.NORMAL,
      draftRevision: 2,
      link: '',
      sensitiveContentList: []
    }

    const saved = await saveBlogVersion(post, 7, form)

    expect(post).toHaveBeenCalledOnce()
    expect(post).toHaveBeenCalledWith('/sys/blog/7/versions', form)
    expect(saved.draftRevision).toBe(3)
    expect(saved.version.id).toBe(9)
  })
})
