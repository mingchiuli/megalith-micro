import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath, pathToFileURL } from 'node:url'
import compression from 'compression'
import express from 'express'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const production = process.env.NODE_ENV === 'production'
const port = Number(process.env.PORT || 1919)
const app = express()
app.disable('x-powered-by')
app.use(compression())

let vite
let productionTemplate = ''
let ssrManifest = {}

if (production) {
  productionTemplate = await fs.readFile(path.join(root, 'dist/client/index.html'), 'utf-8')
  ssrManifest = JSON.parse(
    await fs.readFile(path.join(root, 'dist/client/.vite/ssr-manifest.json'), 'utf-8')
  )
  app.use(
    '/assets',
    express.static(path.join(root, 'dist/client/assets'), {
      immutable: true,
      maxAge: '1y'
    })
  )
  app.use(express.static(path.join(root, 'dist/client'), { index: false, maxAge: '1h' }))
} else {
  const { createServer } = await import('vite')
  vite = await createServer({
    root,
    server: { middlewareMode: true },
    appType: 'custom'
  })
  app.use(vite.middlewares)
}

app.get('/actuator/health', (_request, response) => {
  response.type('text/plain').send('OK')
})

app.use(async (request, response, next) => {
  try {
    const url = request.originalUrl
    let template
    let render
    if (production) {
      template = productionTemplate
      ;({ render } = await import(pathToFileURL(path.join(root, 'dist/server/entry-server.js'))))
    } else {
      template = await fs.readFile(path.join(root, 'index.html'), 'utf-8')
      template = await vite.transformIndexHtml(url, template)
      ;({ render } = await vite.ssrLoadModule('/src/entry-server.ts'))
    }

    const result = await render(url, {
      cookie: request.headers.cookie,
      acceptLanguage: request.headers['accept-language'],
      origin: process.env.APP_ORIGIN || `${request.protocol}://${request.get('host')}`,
      apiBaseURL: process.env.SSR_API_BASE_URL || 'http://127.0.0.1:8088'
    })

    if (result.setCookies.length) response.append('Set-Cookie', result.setCookies)
    if (result.redirect) {
      response.redirect(result.status, result.redirect)
      return
    }

    const preloadLinks = renderPreloadLinks(result.modules, ssrManifest)
    const htmlAttrs = result.htmlAttrs.trim()
    const bodyAttrs = result.bodyAttrs.trim()
    const html = template
      .replace('<html lang="en">', `<html ${htmlAttrs || 'lang="en"'}>`)
      .replace('<body>', bodyAttrs ? `<body ${bodyAttrs}>` : '<body>')
      .replace('<!--app-head-->', `${result.headTags}${preloadLinks}`)
      .replace('<!--app-html-->', result.appHtml)
      .replace(
        '<!--app-state-->',
        `<script id="__MEGALITH_STATE__" type="application/json">${result.state}</script>`
      )
      .replace('<!--app-body-tags-->', result.bodyTags)

    response.status(result.status).set('Cache-Control', 'private, no-store').type('html').send(html)
  } catch (error) {
    vite?.ssrFixStacktrace(error)
    next(error)
  }
})

app.use((error, _request, response, _next) => {
  void _next
  console.error(error)
  response.status(500).type('html').send('<h1>Internal Server Error</h1>')
})

app.listen(port, '0.0.0.0', () => {
  console.log(`Megalith SSR listening on http://0.0.0.0:${port}`)
})

function renderPreloadLinks(modules, manifest) {
  const files = new Set()
  for (const id of modules) {
    for (const file of manifest[id] || []) files.add(file)
  }
  return [...files]
    .map((file) => {
      const href = file.startsWith('/') ? file : `/${file}`
      if (file.endsWith('.js')) return `<link rel="modulepreload" crossorigin href="${href}">`
      if (file.endsWith('.css')) return `<link rel="stylesheet" href="${href}">`
      return ''
    })
    .join('')
}
