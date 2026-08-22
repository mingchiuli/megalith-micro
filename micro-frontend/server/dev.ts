import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import type { IncomingMessage, ServerResponse } from 'node:http'
import type { Plugin, ViteDevServer } from 'vite'

const root = path.resolve(fileURLToPath(new URL('..', import.meta.url)))

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
  return Promise.all([import('./logger.js'), import('./observability.js'), import('./ssr.js')])
}

const installSsrMiddleware = (vite: ViteDevServer) => {
  let runtimePromise: ReturnType<typeof createRuntime> | undefined
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
              ssrManifest: {}
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
