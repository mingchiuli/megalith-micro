import path from 'node:path'
import { logger } from './logger.js'
import { observeHttpRequest } from './observability.js'
import type { Render, SsrResponse } from './ssr.js'
import { renderSsrPage } from './ssr.js'
import { shutdownTelemetry } from './telemetry.js'

type AssetManifest = Record<string, string>

type HttpResult = {
  status: number
  route: string
  headers: Headers
  body: string | Blob
  size?: number
}

const acceptsGzip = (request: Request): boolean => {
  const accepted = (request.headers.get('accept-encoding') || '').split(',').map((part) => {
    const [encoding, ...parameters] = part.trim().toLowerCase().split(';')
    const qualityParameter = parameters.find((parameter) => parameter.trim().startsWith('q='))
    const quality = qualityParameter ? Number(qualityParameter.trim().slice(2)) : 1
    return { encoding, quality: Number.isFinite(quality) ? quality : 0 }
  })
  const gzip = accepted.find(({ encoding }) => encoding === 'gzip')
  if (gzip) return gzip.quality > 0
  return (accepted.find(({ encoding }) => encoding === '*')?.quality ?? 0) > 0
}

const isCompressible = (contentType: string): boolean =>
  /^(text\/|application\/(javascript|json|xml|svg\+xml)|image\/svg\+xml)/i.test(contentType)

const appendVary = (headers: Headers, value: string) => {
  const current = headers.get('Vary')
  if (!current) headers.set('Vary', value)
  else if (
    !current
      .toLowerCase()
      .split(/\s*,\s*/)
      .includes(value.toLowerCase())
  ) {
    headers.set('Vary', `${current}, ${value}`)
  }
}

const toResponse = (request: Request, result: HttpResult): Response => {
  const method = request.method.toUpperCase()
  const contentType = result.headers.get('Content-Type') || ''
  const bodySize =
    result.size ??
    (typeof result.body === 'string' ? Buffer.byteLength(result.body) : result.body.size)
  const compress = acceptsGzip(request) && isCompressible(contentType) && bodySize >= 1024

  if (compress) {
    result.headers.set('Content-Encoding', 'gzip')
    result.headers.delete('Content-Length')
    appendVary(result.headers, 'Accept-Encoding')
  }

  if (method === 'HEAD')
    return new Response(null, { status: result.status, headers: result.headers })
  if (!compress)
    return new Response(result.body, { status: result.status, headers: result.headers })

  const source = typeof result.body === 'string' ? new Blob([result.body]) : result.body
  const body = source.stream().pipeThrough(new CompressionStream('gzip'))
  return new Response(body, { status: result.status, headers: result.headers })
}

const pageResult = (result: SsrResponse): HttpResult => ({
  status: result.status,
  route: result.route,
  headers: new Headers(result.headers),
  body: result.body,
  size: Buffer.byteLength(result.body)
})

export const startProductionServer = async (render: Render): Promise<void> => {
  const clientRoot = path.join(import.meta.dir, 'client')
  const [template, ssrManifest, assetManifest] = await Promise.all([
    Bun.file(path.join(clientRoot, 'index.html')).text(),
    Bun.file(path.join(clientRoot, '.vite/ssr-manifest.json')).json() as Promise<
      Record<string, string[]>
    >,
    Bun.file(path.join(clientRoot, '.vite/public-assets.json')).json() as Promise<AssetManifest>
  ])
  let shuttingDown = false

  const health = (request: Request): Response => {
    const body = shuttingDown ? 'STOPPING' : 'OK'
    return new Response(request.method === 'HEAD' ? null : body, {
      status: shuttingDown ? 503 : 200,
      headers: { 'Content-Type': 'text/plain; charset=utf-8' }
    })
  }

  const server = Bun.serve({
    hostname: '0.0.0.0',
    port: Number(process.env.PORT || 1919),
    development: false,
    routes: {
      '/actuator/health': {
        GET: health,
        HEAD: health
      }
    },
    async fetch(request) {
      const result = await observeHttpRequest(
        { method: request.method, url: request.url, headers: request.headers },
        async (): Promise<HttpResult> => {
          const url = new URL(request.url)
          const relativeAsset = assetManifest[url.pathname]
          if ((request.method === 'GET' || request.method === 'HEAD') && relativeAsset) {
            const file = Bun.file(path.join(clientRoot, relativeAsset))
            const headers = new Headers({
              'Cache-Control': url.pathname.startsWith('/assets/')
                ? 'public, max-age=31536000, immutable'
                : 'public, max-age=3600',
              'Content-Length': String(file.size),
              'Content-Type': file.type || 'application/octet-stream'
            })
            return {
              status: 200,
              route: url.pathname.startsWith('/assets/') ? '/assets/*' : url.pathname,
              headers,
              body: file,
              size: file.size
            }
          }

          const rendered = await renderSsrPage(
            { method: request.method, url: request.url, headers: request.headers },
            {
              loadTemplate: async () => template,
              loadRender: async () => render,
              ssrManifest
            }
          )
          return pageResult(rendered)
        }
      )
      return toResponse(request, result)
    },
    error(error) {
      logger.error('SSR request failed', error)
      return new Response('<h1>Internal Server Error</h1>', {
        status: 500,
        headers: { 'Content-Type': 'text/html; charset=utf-8' }
      })
    }
  })

  logger.info('Megalith SSR server started', {
    'server.address': server.hostname,
    'server.port': server.port,
    'runtime.name': 'bun',
    'runtime.version': Bun.version,
    'runtime.standalone': Bun.isStandaloneExecutable
  })

  let shutdownPromise: Promise<void> | undefined
  const shutdown = (signal: NodeJS.Signals): Promise<void> => {
    if (shutdownPromise) return shutdownPromise
    shuttingDown = true
    logger.info('Shutdown started', { 'process.signal': signal })

    const forceCloseTimer = setTimeout(() => void server.stop(true), 10_000)
    forceCloseTimer.unref()
    shutdownPromise = server
      .stop()
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

  for (const signal of ['SIGTERM', 'SIGINT'] as const) {
    process.once(signal, () => void shutdown(signal))
  }
}
