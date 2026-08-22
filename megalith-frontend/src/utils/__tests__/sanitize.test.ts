import { describe, expect, it } from 'vitest'
import { sanitizeHighlight, sanitizeHtml } from '../sanitize'

describe('universal HTML sanitizer', () => {
  it('preserves the safe markup used by rendered markdown', () => {
    const clean = sanitizeHtml(
      '<h2 id="intro">Intro</h2><pre><code class="language-ts" data-line="1">code</code></pre>' +
        '<a href="https://example.com" target="_blank" rel="noopener">link</a>' +
        '<img src="https://example.com/image.png" alt="preview" loading="lazy">'
    )

    expect(clean).toContain('<h2 id="intro">Intro</h2>')
    expect(clean).toContain('class="language-ts"')
    expect(clean).toContain('data-line="1"')
    expect(clean).toContain('target="_blank"')
    expect(clean).toContain('loading="lazy"')
  })

  it('removes executable and untrusted markup', () => {
    const clean = sanitizeHtml(
      '<script>alert(1)</script><style>body{display:none}</style>' +
        '<iframe src="https://example.com"></iframe><p onclick="alert(1)" style="color:red" data-secret="x">' +
        '<a href="javascript:alert(1)">unsafe</a><img src="x" onerror="alert(1)">safe</p>'
    )

    expect(clean).not.toMatch(/script|style|iframe|onclick|onerror|javascript:|data-secret/i)
    expect(clean).toContain('safe')
  })

  it('preserves surrounding text and restricts highlights to inline emphasis', () => {
    const clean = sanitizeHighlight(
      'Title: <em class="result">match</em> &amp; text<p>nested block</p><script>x</script>'
    )

    expect(clean).toBe('Title: <em>match</em> &amp; textnested block')
  })
})
