import type { Attributes, TextMapGetter } from '@opentelemetry/api'
import {
  metrics,
  propagation,
  ROOT_CONTEXT,
  SpanKind,
  SpanStatusCode,
  trace
} from '@opentelemetry/api'
import {
  ATTR_ERROR_TYPE,
  ATTR_HTTP_REQUEST_METHOD,
  ATTR_HTTP_RESPONSE_STATUS_CODE,
  ATTR_HTTP_ROUTE,
  ATTR_SERVER_ADDRESS,
  ATTR_SERVER_PORT,
  ATTR_URL_PATH,
  ATTR_URL_QUERY,
  ATTR_URL_SCHEME
} from '@opentelemetry/semantic-conventions'
import { serviceName, serviceVersion } from './telemetry.js'

type RenderResult = { route: string; status: number }

const ssrTracer = trace.getTracer(`${serviceName}.ssr`, serviceVersion)
const serverTracer = trace.getTracer(`${serviceName}.http`, serviceVersion)
const meter = metrics.getMeter(`${serviceName}.server`, serviceVersion)
const renderDuration = meter.createHistogram('ssr.render.duration', {
  description: 'Duration of Vue server-side rendering',
  unit: 's'
})
const renderRequests = meter.createCounter('ssr.render.requests', {
  description: 'Number of Vue server-side render attempts'
})
const renderErrors = meter.createCounter('ssr.render.errors', {
  description: 'Number of failed Vue server-side renders'
})
const activeRenders = meter.createUpDownCounter('ssr.render.active', {
  description: 'Number of Vue server-side renders in progress'
})
const requestDuration = meter.createHistogram('http.server.request.duration', {
  description: 'Duration of inbound HTTP requests handled by Bun.serve',
  unit: 's'
})
const activeRequests = meter.createUpDownCounter('http.server.active_requests', {
  description: 'Number of active inbound HTTP requests handled by Bun.serve'
})

const errorType = (error: unknown): string => (error instanceof Error ? error.name : typeof error)

export const observeSsrRender = <T extends RenderResult>(
  method: string,
  render: () => Promise<T>
): Promise<T> => {
  const spanAttributes = {
    [ATTR_HTTP_REQUEST_METHOD]: method
  }

  return ssrTracer.startActiveSpan('ssr.render', { attributes: spanAttributes }, async (span) => {
    const startedAt = Bun.nanoseconds()
    activeRenders.add(1)
    let metricAttributes: Attributes = {
      [ATTR_HTTP_REQUEST_METHOD]: method,
      [ATTR_HTTP_ROUTE]: 'unknown',
      [ATTR_HTTP_RESPONSE_STATUS_CODE]: 500
    }

    try {
      const result = await render()
      metricAttributes = {
        [ATTR_HTTP_REQUEST_METHOD]: method,
        [ATTR_HTTP_ROUTE]: result.route,
        [ATTR_HTTP_RESPONSE_STATUS_CODE]: result.status
      }
      span.updateName(`ssr.render ${result.route}`)
      span.setAttributes(metricAttributes)
      if (result.status >= 500) span.setStatus({ code: SpanStatusCode.ERROR })
      renderRequests.add(1, metricAttributes)
      return result
    } catch (error) {
      const type = errorType(error)
      metricAttributes[ATTR_ERROR_TYPE] = type
      span.setAttribute(ATTR_ERROR_TYPE, type)
      span.setStatus({ code: SpanStatusCode.ERROR })
      span.recordException(error instanceof Error ? error : String(error))
      renderRequests.add(1, metricAttributes)
      renderErrors.add(1, metricAttributes)
      throw error
    } finally {
      activeRenders.add(-1)
      renderDuration.record((Bun.nanoseconds() - startedAt) / 1_000_000_000, metricAttributes)
      span.end()
    }
  })
}

type HttpRequest = {
  method: string
  url: string
  headers: Headers
}

type HttpResult = {
  status: number
  route: string
}

const headerGetter: TextMapGetter<Headers> = {
  keys: (headers) => [...headers.keys()],
  get: (headers, key) => headers.get(key) || undefined
}

export const observeHttpRequest = <T extends HttpResult>(
  request: HttpRequest,
  handle: () => Promise<T>
): Promise<T> => {
  const url = new URL(request.url)
  const method = request.method.toUpperCase()
  const port = url.port ? Number(url.port) : url.protocol === 'https:' ? 443 : 80
  const attributes: Attributes = {
    [ATTR_HTTP_REQUEST_METHOD]: method,
    [ATTR_SERVER_ADDRESS]: url.hostname,
    [ATTR_SERVER_PORT]: port,
    [ATTR_URL_PATH]: url.pathname,
    [ATTR_URL_SCHEME]: url.protocol.slice(0, -1)
  }
  if (url.search) attributes[ATTR_URL_QUERY] = 'REDACTED'

  const parentContext = propagation.extract(ROOT_CONTEXT, request.headers, headerGetter)
  return serverTracer.startActiveSpan(
    method,
    { kind: SpanKind.SERVER, attributes },
    parentContext,
    async (span) => {
      const startedAt = Bun.nanoseconds()
      activeRequests.add(1, { [ATTR_HTTP_REQUEST_METHOD]: method })
      let metricAttributes: Attributes = {
        [ATTR_HTTP_REQUEST_METHOD]: method,
        [ATTR_HTTP_ROUTE]: 'unknown',
        [ATTR_HTTP_RESPONSE_STATUS_CODE]: 500
      }

      try {
        const result = await handle()
        metricAttributes = {
          [ATTR_HTTP_REQUEST_METHOD]: method,
          [ATTR_HTTP_ROUTE]: result.route,
          [ATTR_HTTP_RESPONSE_STATUS_CODE]: result.status
        }
        span.updateName(`${method} ${result.route}`)
        span.setAttributes(metricAttributes)
        if (result.status >= 500) span.setStatus({ code: SpanStatusCode.ERROR })
        return result
      } catch (error) {
        const type = errorType(error)
        metricAttributes[ATTR_ERROR_TYPE] = type
        span.setAttribute(ATTR_ERROR_TYPE, type)
        span.setStatus({ code: SpanStatusCode.ERROR })
        span.recordException(error instanceof Error ? error : String(error))
        throw error
      } finally {
        activeRequests.add(-1, { [ATTR_HTTP_REQUEST_METHOD]: method })
        requestDuration.record((Bun.nanoseconds() - startedAt) / 1_000_000_000, metricAttributes)
        span.end()
      }
    }
  )
}
