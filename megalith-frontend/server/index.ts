import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath, pathToFileURL } from 'node:url'
import compression from 'compression'
import express from 'express'
import type { ErrorRequestHandler } from 'express'
import type { ViteDevServer } from 'vite'
import { logger } from './logger.js'
import { observeSsrRender } from './observability.js'
import { shutdownTelemetry } from './telemetry.js'

type RenderResult = {
  appHtml: string
  headTags: string
  htmlAttrs: string
  bodyAttrs: string
  bodyTags: string
  state: string
  status: number
  route: string
  redirect?: string
  modules: Set<string>
  setCookies: string[]
}

type Render = (
  url: string,
  request: {
    cookie?: string
    acceptLanguage?: string
    origin?: string
    apiBaseURL?: string
  }
) => Promise<RenderResult>

const root = path.resolve(fileURLToPath(new URL('../../..', import.meta.url)))
const production = process.env.NODE_ENV === 'production'
const port = Number(process.env.PORT || 1919)
const app = express()
let shuttingDown = false
app.disable('x-powered-by')
app.use(compression())

let vite: ViteDevServer | undefined
let productionTemplate = ''
let ssrManifest: Record<string, string[]> = {}

if (production) {
  productionTemplate = await fs.readFile(path.join(root, 'dist/client/index.html'), 'utf-8')
  ssrManifest = JSON.parse(
    await fs.readFile(path.join(root, 'dist/client/.vite/ssr-manifest.json'), 'utf-8')
  ) as Record<string, string[]>
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
  response
    .status(shuttingDown ? 503 : 200)
    .type('text/plain')
    .send(shuttingDown ? 'STOPPING' : 'OK')
})

app.use(async (request, response, next) => {
  response.locals.otelRoute = 'unknown'
  try {
    const url = request.originalUrl
    let template: string
    let render: Render
    if (production) {
      template = productionTemplate
      ;({ render } = (await import(
        pathToFileURL(path.join(root, 'dist/server/entry-server.js')).href
      )) as { render: Render })
    } else {
      if (!vite) throw new Error('Vite development server is unavailable')
      template = await fs.readFile(path.join(root, 'index.html'), 'utf-8')
      template = await vite.transformIndexHtml(url, template)
      ;({ render } = (await vite.ssrLoadModule('/src/entry-server.ts')) as { render: Render })
    }

    const result = await observeSsrRender(request, () =>
      render(url, {
        cookie: request.headers.cookie,
        acceptLanguage: request.headers['accept-language'],
        origin: process.env.APP_ORIGIN || `${request.protocol}://${request.get('host')}`,
        apiBaseURL: process.env.SSR_API_BASE_URL || 'http://127.0.0.1:8088'
      })
    )
    response.locals.otelRoute = result.route

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
    if (error instanceof Error) vite?.ssrFixStacktrace(error)
    next(error)
  }
})

const errorHandler: ErrorRequestHandler = (error, _request, response, _next) => {
  void _next
  logger.error('SSR request failed', error)
  return response.status(500).type('html').send('<h1>Internal Server Error</h1>')
}
app.use(errorHandler)

const server = app.listen(port, '0.0.0.0', () => {
  logger.info('Megalith SSR server started', {
    'server.address': '0.0.0.0',
    'server.port': port
  })
})

const closeServer = (): Promise<void> =>
  new Promise((resolve, reject) => {
    server.close((error) => (error ? reject(error) : resolve()))
  })

let shutdownPromise: Promise<void> | undefined
const shutdown = (signal: NodeJS.Signals): Promise<void> => {
  if (shutdownPromise) return shutdownPromise
  shuttingDown = true
  logger.info('Shutdown started', { 'process.signal': signal })

  const forceCloseTimer = setTimeout(() => server.closeAllConnections(), 10_000)
  forceCloseTimer.unref()
  shutdownPromise = closeServer()
    .then(() => vite?.close())
    .then(() => {
      clearTimeout(forceCloseTimer)
      logger.info('HTTP server stopped')
      return shutdownTelemetry()
    })
    .catch(async (error: unknown) => {
      logger.error('Graceful shutdown failed', error)
      process.exitCode = 1
      await shutdownTelemetry().catch(() => undefined)
    })

  return shutdownPromise
}

const shutdownSignals: NodeJS.Signals[] = ['SIGTERM', 'SIGINT']
for (const signal of shutdownSignals) {
  process.once(signal, () => void shutdown(signal))
}

function renderPreloadLinks(modules: Set<string>, manifest: Record<string, string[]>): string {
  const files = new Set<string>()
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
