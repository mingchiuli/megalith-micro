import assert from 'node:assert/strict'
import { render } from '../dist/server/entry-server.js'

const request = {
  origin: 'https://chiu.wiki',
  apiBaseURL: 'http://127.0.0.1:1',
  acceptLanguage: 'zh-CN'
}

const login = await render('/login', request)
assert.equal(login.status, 200)
assert.match(login.appHtml, /class="front"/)
assert.match(login.headTags, /<title>登录<\/title>/)

const notFound = await render('/production-ssr-smoke-not-found', request)
assert.equal(notFound.status, 404)
assert.match(notFound.appHtml, /404 NOT FOUND/)

console.log('Production SSR bundle rendered successfully')
