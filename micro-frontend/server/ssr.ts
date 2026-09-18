import { logger } from './logger.js'
import { observeSsrRender } from './observability.js'

export type RenderResult = {
  appHtml: string
  headTags: string
  htmlAttrs: string
  bodyAttrs: string
  bodyTags: string
  teleports: Record<string, string>
  state: string
  status: number
  route: string
  redirect?: string
  modules: Set<string>
  setCookies: string[]
  prefetchFailures?: Array<{ key: string; reason: unknown }>
}

export type Render = (
  url: string,
  request: {
    cookie?: string
    acceptLanguage?: string
    origin?: string
    apiBaseURL?: string
  }
) => Promise<RenderResult>

export type SsrRequest = {
  method: string
  url: string
  headers: Headers
}

export type SsrResponse = {
  status: number
  route: string
  headers: Array<[string, string]>
  body: string
}

export type ClientManifestChunk = {
  file: string
  css?: string[]
  imports?: string[]
}

export type ClientManifest = Record<string, ClientManifestChunk>

type SsrDependencies = {
  loadTemplate: (url: string) => Promise<string>
  loadRender: () => Promise<Render>
  ssrManifest: Record<string, string[]>
  clientManifest?: ClientManifest
  /**
   * Development-only hook: the Vite dev server resolves styles through its module graph
   * because there is no build manifest to expand.
   */
  headStyles?: (modules: Set<string>, alreadyLinked: Set<string>) => Promise<string>
}

export const renderSsrPage = async (
  request: SsrRequest,
  dependencies: SsrDependencies
): Promise<SsrResponse> => {
  const parsedUrl = new URL(request.url)
  const pageUrl = `${parsedUrl.pathname}${parsedUrl.search}`
  const [template, render] = await Promise.all([
    dependencies.loadTemplate(pageUrl),
    dependencies.loadRender()
  ])

  const result = await observeSsrRender(request.method, () =>
    render(pageUrl, {
      cookie: request.headers.get('cookie') || undefined,
      acceptLanguage: request.headers.get('accept-language') || undefined,
      origin: process.env.APP_ORIGIN || parsedUrl.origin,
      apiBaseURL: process.env.SSR_API_BASE_URL || 'http://127.0.0.1:8088'
    })
  )

  const headers: Array<[string, string]> = []
  for (const cookie of result.setCookies) headers.push(['Set-Cookie', cookie])

  if (result.redirect) {
    headers.push(
      ['Location', result.redirect],
      ['Cache-Control', 'private, no-store'],
      ['Content-Type', 'text/plain; charset=utf-8']
    )
    return {
      status: result.status,
      route: result.route,
      headers,
      body: `Redirecting to ${result.redirect}`
    }
  }

  const alreadyLinked = linkedAssets(template)
  const preloadLinks = renderPreloadLinks(
    result.modules,
    dependencies.ssrManifest,
    dependencies.clientManifest,
    alreadyLinked
  )
  const additionalStyles = dependencies.headStyles
    ? await dependencies.headStyles(result.modules, alreadyLinked)
    : ''
  for (const failure of result.prefetchFailures ?? []) {
    logger.error('SSR prefetch failed', failure.reason, {
      'ssr.route': result.route,
      'ssr.prefetch.key': failure.key
    })
  }
  const htmlAttrs = result.htmlAttrs.trim()
  const bodyAttrs = result.bodyAttrs.trim()
  const html = template
    .replace('<html lang="en">', `<html ${htmlAttrs || 'lang="en"'}>`)
    .replace('<body>', bodyAttrs ? `<body ${bodyAttrs}>` : '<body>')
    .replace('<!--app-head-->', `${result.headTags}${preloadLinks}${additionalStyles}`)
    .replace('<!--app-html-->', result.appHtml)
    .replace(
      '<!--app-state-->',
      `<script id="__MEGALITH_STATE__" type="application/json">${result.state}</script>`
    )
    .replace('<!--app-teleports-->', renderTeleports(result.teleports))
    .replace('<!--app-body-tags-->', result.bodyTags)

  headers.push(['Cache-Control', 'private, no-store'], ['Content-Type', 'text/html; charset=utf-8'])
  return { status: result.status, route: result.route, headers, body: html }
}

function renderTeleports(teleports: Record<string, string>): string {
  return Object.entries(teleports).reduce((html, [target, content]) => {
    if (target.startsWith('#el-popper-container-')) {
      return `${html}<div id="${target.slice(1)}">${content}</div>`
    }
    return html
  }, teleports.body ?? '')
}

const linkedAssets = (template: string): Set<string> => {
  const hrefs = new Set<string>()
  for (const [, href] of template.matchAll(/<link\b[^>]*\bhref="([^"]+)"/g)) {
    if (href) hrefs.add(href)
  }
  return hrefs
}

const manifestKey = (file: string): string => (file.startsWith('/') ? file.slice(1) : file)

/**
 * The SSR manifest only maps a rendered component to its own chunk. Component styles
 * imported by that chunk are declared in the client manifest instead, so the route has
 * to be expanded transitively. Without them the browser paints the server HTML before
 * the component CSS arrives, which flashes unstyled controls on slow connections.
 */
function renderPreloadLinks(
  modules: Set<string>,
  ssrManifest: Record<string, string[]>,
  clientManifest?: ClientManifest,
  alreadyLinked: Set<string> = new Set<string>()
): string {
  const files: string[] = []
  const queued = new Set<string>()
  const visitedChunks = new Set<string>()
  const keyByFile = new Map<string, string>()
  for (const [key, chunk] of Object.entries(clientManifest ?? {})) {
    keyByFile.set(manifestKey(chunk.file), key)
  }

  const enqueue = (file: string) => {
    const normalized = manifestKey(file)
    if (queued.has(normalized)) return
    queued.add(normalized)
    files.push(normalized)
  }

  const visitChunk = (key: string) => {
    if (visitedChunks.has(key)) return
    const chunk = clientManifest?.[key]
    if (!chunk) return
    visitedChunks.add(key)
    enqueue(chunk.file)
    for (const css of chunk.css ?? []) enqueue(css)
    for (const imported of chunk.imports ?? []) visitChunk(imported)
  }

  for (const id of modules) {
    for (const file of ssrManifest[id] ?? []) {
      enqueue(file)
      const key = keyByFile.get(manifestKey(file))
      if (key !== undefined) visitChunk(key)
    }
  }

  return files
    .map((file) => (file.startsWith('/') ? file : `/${file}`))
    .filter((href) => !alreadyLinked.has(href))
    .map((href) => {
      if (href.endsWith('.js')) return `<link rel="modulepreload" crossorigin href="${href}">`
      if (href.endsWith('.css')) return `<link rel="stylesheet" crossorigin href="${href}">`
      return ''
    })
    .join('')
}
