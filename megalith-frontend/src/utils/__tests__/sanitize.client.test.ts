import { beforeEach, describe, expect, it, vi } from 'vitest'

const mocks = vi.hoisted(() => ({
  sanitize: vi.fn((html: string) => `clean:${html}`)
}))

vi.mock('dompurify', () => ({ default: mocks }))

import { sanitizeHighlight, sanitizeHtml } from '../sanitize.client'

describe('client HTML sanitizer', () => {
  beforeEach(() => mocks.sanitize.mockClear())

  it('uses DOMPurify with the hardened application policy', () => {
    expect(sanitizeHtml('<p>content</p>')).toBe('clean:<p>content</p>')
    expect(mocks.sanitize).toHaveBeenCalledWith(
      '<p>content</p>',
      expect.objectContaining({
        ALLOW_DATA_ATTR: false,
        FORBID_ATTR: ['style'],
        FORBID_TAGS: ['style', 'iframe', 'object', 'embed'],
        USE_PROFILES: { html: true }
      })
    )
  })

  it('applies the same policy to highlighted search results', () => {
    expect(sanitizeHighlight('<em>result</em>')).toBe('clean:<em>result</em>')
    expect(mocks.sanitize).toHaveBeenCalledOnce()
  })
})
