import { describe, expect, it } from 'vitest'
import { render } from '@/entry-server'

const request = {
  origin: 'https://chiu.wiki',
  apiBaseURL: 'http://127.0.0.1:1',
  acceptLanguage: 'zh-CN'
}

describe('SSR entry', () => {
  it('renders a public route and serializes request state', async () => {
    const result = await render('/login', request)

    expect(result.status).toBe(200)
    expect(result.redirect).toBeUndefined()
    expect(result.appHtml).toContain('class="front"')
    expect(result.headTags).toContain('<title>\u767b\u5f55</title>')
    expect(result.htmlAttrs).toContain('lang="zh-CN"')
    expect(result.state).toContain('loginStateStore')
  })

  it('returns rendered HTML with a 404 status for unknown routes', async () => {
    const result = await render('/does-not-exist', request)

    expect(result.status).toBe(404)
    expect(result.appHtml).toContain('404 NOT FOUND')
  })
})
