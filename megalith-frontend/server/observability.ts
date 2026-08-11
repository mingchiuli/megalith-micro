import { performance } from 'node:perf_hooks'
import type { Attributes } from '@opentelemetry/api'
import { metrics, SpanStatusCode, trace } from '@opentelemetry/api'
import {
  ATTR_ERROR_TYPE,
  ATTR_HTTP_REQUEST_METHOD,
  ATTR_HTTP_RESPONSE_STATUS_CODE,
  ATTR_HTTP_ROUTE
} from '@opentelemetry/semantic-conventions'
import type { Request } from 'express'
import { serviceName, serviceVersion } from './telemetry.js'

type RenderResult = { route: string; status: number }

const tracer = trace.getTracer(`${serviceName}.ssr`, serviceVersion)
const meter = metrics.getMeter(`${serviceName}.ssr`, serviceVersion)
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

const errorType = (error: unknown): string => (error instanceof Error ? error.name : typeof error)

export const observeSsrRender = <T extends RenderResult>(
  request: Request,
  render: () => Promise<T>
): Promise<T> => {
  const method = request.method
  const spanAttributes = {
    [ATTR_HTTP_REQUEST_METHOD]: method
  }

  return tracer.startActiveSpan('ssr.render', { attributes: spanAttributes }, async (span) => {
    const startedAt = performance.now()
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
      renderDuration.record((performance.now() - startedAt) / 1000, metricAttributes)
      span.end()
    }
  })
}
