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

type SsrDependencies = {
  loadTemplate: (url: string) => Promise<string>
  loadRender: () => Promise<Render>
  ssrManifest: Record<string, string[]>
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

  const preloadLinks = renderPreloadLinks(result.modules, dependencies.ssrManifest)
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
