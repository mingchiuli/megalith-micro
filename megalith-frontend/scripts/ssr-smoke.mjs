import assert from 'node:assert/strict'
import { createServer } from 'node:http'
import { render } from '../dist/server/entry-server.js'

const apiServer = createServer((request, response) => {
  response.setHeader('Content-Type', 'application/json')
  if (request.url === '/public/blog/stat') {
    response.end(
      JSON.stringify({
        msg: 'OK',
        data: { dayVisit: 1, weekVisit: 2, monthVisit: 3, yearVisit: 4 }
      })
    )
    return
  }
  response.statusCode = 404
  response.end(JSON.stringify({ msg: 'Not Found', data: null }))
})
await new Promise((resolve) => apiServer.listen(0, '127.0.0.1', resolve))
const apiAddress = apiServer.address()
assert.ok(apiAddress && typeof apiAddress === 'object')

const request = {
  origin: 'https://chiu.wiki',
  apiBaseURL: `http://127.0.0.1:${apiAddress.port}`,
  acceptLanguage: 'zh-CN'
}

try {
  const intro = await render('/', request)
  assert.equal(intro.status, 200)
  assert.match(intro.appHtml, /class="intro-notebook-icon"/)
  assert.match(intro.appHtml, /class="intro-github-icon"/)

  const login = await render('/login', request)
  assert.equal(login.status, 200)
  assert.match(login.appHtml, /class="front"/)
  assert.match(login.headTags, /<title>登录<\/title>/)

  const notFound = await render('/production-ssr-smoke-not-found', request)
  assert.equal(notFound.status, 404)
  assert.match(notFound.appHtml, /404 NOT FOUND/)

  console.log('Production SSR bundle rendered successfully')
} finally {
  await new Promise((resolve, reject) =>
    apiServer.close((error) => (error ? reject(error) : resolve()))
  )
}
