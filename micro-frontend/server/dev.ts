import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import type { IncomingMessage, ServerResponse } from 'node:http'
import type { Plugin, ViteDevServer } from 'vite'

const root = path.resolve(fileURLToPath(new URL('..', import.meta.url)))

const styleCache = new Map<string, string[]>()
let entryStyleCache: string[] | undefined

const isStyleModule = (id: string): boolean => /\.css($|\?)/.test(id) || /[?&]type=style/.test(id)

const moduleUrl = (id: string, url?: string | null): string => {
  const value = url ?? id
  return value.startsWith('/') ? value : `/${value}`
}

/**
 * Vite injects styles through JavaScript during development, so a server-rendered page would
 * paint unstyled until the client entry executes. Walking the SSR module graph gives the same
 * stylesheets the production manifest declares, and Vite serves each of them as text/css when a
 * stylesheet link requests it.
 */
const collectRouteStyleUrls = (vite: ViteDevServer, modules: Set<string>): string[] => {
  const graph = vite.environments.ssr.moduleGraph
  const urls: string[] = []
  const push = (url: string) => {
    if (!urls.includes(url)) urls.push(url)
  }

  for (const id of modules) {
    const absolute = path.isAbsolute(id) ? id : path.resolve(root, id)
    const cached = styleCache.get(absolute)
    if (cached) {
      cached.forEach(push)
      continue
    }

    const discovered: string[] = []
    const visited = new Set<string>()
    const walk = (moduleId: string, depth: number) => {
      if (depth > 8 || visited.has(moduleId)) return
      visited.add(moduleId)
      const module = graph.getModuleById(moduleId)
      if (!module) return
      const styleId = module.id
      if (styleId && isStyleModule(styleId)) {
        const url = moduleUrl(styleId, module.url)
        if (!discovered.includes(url)) discovered.push(url)
      }
      for (const imported of module.importedModules) {
        if (imported.id) walk(imported.id, depth + 1)
      }
    }
    walk(absolute, 0)
    styleCache.set(absolute, discovered)
    discovered.forEach(push)
  }

  return urls
}

const collectEntryStyleUrls = async (vite: ViteDevServer): Promise<string[]> => {
  if (entryStyleCache) return entryStyleCache
  const transformed = await vite.transformRequest('/src/entry-client.ts')
  const urls: string[] = []
  for (const [, specifier] of (transformed?.code ?? '').matchAll(/import\s+"([^"]+\.css)"/g)) {
    if (!specifier) continue
    const url = specifier.startsWith('/') ? specifier : `/${specifier}`
    if (!urls.includes(url)) urls.push(url)
  }
  entryStyleCache = urls
  return urls
}

const renderStyleLinks = (urls: string[], alreadyLinked: Set<string>): string => {
  const seen = new Set(alreadyLinked)
  const links: string[] = []
  for (const url of urls) {
    if (seen.has(url)) continue
    seen.add(url)
    links.push(`<link rel="stylesheet" href="${url}">`)
  }
  return links.join('')
}

const requestUrl = (request: IncomingMessage): string => {
  const host = request.headers.host || '127.0.0.1:1919'
  return new URL(request.url || '/', `http://${host}`).href
}

const writeResponse = (
  request: IncomingMessage,
  response: ServerResponse,
  result: { status: number; headers: Array<[string, string]>; body: string }
) => {
  response.statusCode = result.status
  const headers = new Map<string, string[]>()
  for (const [name, value] of result.headers) {
    const values = headers.get(name) || []
    values.push(value)
    headers.set(name, values)
  }
  for (const [name, values] of headers) {
    response.setHeader(name, name.toLowerCase() === 'set-cookie' ? values : values.join(', '))
  }
  response.end(request.method === 'HEAD' ? undefined : result.body)
}

const createRuntime = async () => {
  await import('./telemetry.js')
  const [runtime, guards] = await Promise.all([
    Promise.all([import('./logger.js'), import('./observability.js'), import('./ssr.js')]),
    import('./process-guards.js')
  ])
  guards.installProcessGuards()
  return runtime
}

const installSsrMiddleware = (vite: ViteDevServer) => {
  let runtimePromise: ReturnType<typeof createRuntime> | undefined
  const invalidateStyleCache = () => {
    styleCache.clear()
    entryStyleCache = undefined
  }
  vite.watcher.on('change', invalidateStyleCache)
  vite.watcher.on('add', invalidateStyleCache)
  vite.watcher.on('unlink', invalidateStyleCache)

  vite.middlewares.use(async (request, response) => {
    const absoluteUrl = requestUrl(request)
    const url = new URL(absoluteUrl)
    if (
      url.pathname === '/actuator/health' &&
      (request.method === 'GET' || request.method === 'HEAD')
    ) {
      response.statusCode = 200
      response.setHeader('Content-Type', 'text/plain; charset=utf-8')
      response.end(request.method === 'HEAD' ? undefined : 'OK')
      return
    }

    runtimePromise ||= createRuntime()
    const [{ logger }, { observeHttpRequest }, { renderSsrPage }] = await runtimePromise
    try {
      const headers = new Headers()
      for (const [name, value] of Object.entries(request.headers)) {
        if (Array.isArray(value)) for (const item of value) headers.append(name, item)
        else if (value !== undefined) headers.set(name, value)
      }

      const result = await observeHttpRequest(
        { method: request.method || 'GET', url: absoluteUrl, headers },
        async () =>
          renderSsrPage(
            { method: request.method || 'GET', url: absoluteUrl, headers },
            {
              loadTemplate: async (pageUrl) => {
                const source = await fs.readFile(path.join(root, 'index.html'), 'utf-8')
                return vite.transformIndexHtml(pageUrl, source)
              },
              loadRender: async () => {
                const module = (await vite.ssrLoadModule('/src/entry-server.ts')) as {
                  render: import('./ssr.js').Render
                }
                return module.render
              },
              ssrManifest: {},
              headStyles: async (modules, alreadyLinked) =>
                renderStyleLinks(
                  [...collectRouteStyleUrls(vite, modules), ...(await collectEntryStyleUrls(vite))],
                  alreadyLinked
                )
            }
          )
      )
      writeResponse(request, response, result)
    } catch (error) {
      if (error instanceof Error) vite.ssrFixStacktrace(error)
      logger.error('SSR request failed', error)
      response.statusCode = 500
      response.setHeader('Content-Type', 'text/html; charset=utf-8')
      response.end('<h1>Internal Server Error</h1>')
    }
  })
}

export const megalithSsrDevPlugin = (): Plugin => ({
  name: 'megalith-ssr-dev-server',
  apply: 'serve',
  configureServer(vite) {
    return () => installSsrMiddleware(vite)
  }
})
