import { createRequire } from 'node:module'
import { fileURLToPath } from 'node:url'
import process from 'node:process'
import { ExpressInstrumentation, ExpressLayerType } from '@opentelemetry/instrumentation-express'
import { HttpInstrumentation } from '@opentelemetry/instrumentation-http'
import { RuntimeNodeInstrumentation } from '@opentelemetry/instrumentation-runtime-node'
import { defaultResource, resourceFromAttributes } from '@opentelemetry/resources'
import { NodeSDK } from '@opentelemetry/sdk-node'
import {
  ATTR_DEPLOYMENT_ENVIRONMENT_NAME,
  ATTR_HTTP_ROUTE,
  ATTR_SERVICE_NAME,
  ATTR_SERVICE_NAMESPACE,
  ATTR_SERVICE_VERSION,
  ATTR_URL_PATH,
  ATTR_URL_QUERY
} from '@opentelemetry/semantic-conventions'

const require = createRequire(import.meta.url)
const packageJsonPath = fileURLToPath(new URL('../../../package.json', import.meta.url))
const { version } = require(packageJsonPath) as { version: string }

process.env.OTEL_EXPORTER_OTLP_ENDPOINT ||= 'http://127.0.0.1:8200'
process.env.OTEL_EXPORTER_OTLP_PROTOCOL ||= 'http/protobuf'
process.env.OTEL_TRACES_SAMPLER ||= 'parentbased_traceidratio'
process.env.OTEL_TRACES_SAMPLER_ARG ||= '0.5'

export const serviceName = process.env.OTEL_SERVICE_NAME || 'megalith-frontend'
export const serviceVersion = process.env.OTEL_SERVICE_VERSION || version

const sdk = new NodeSDK({
  resource: defaultResource().merge(
    resourceFromAttributes({
      [ATTR_SERVICE_NAME]: serviceName,
      [ATTR_SERVICE_NAMESPACE]: 'megalith',
      [ATTR_SERVICE_VERSION]: serviceVersion,
      [ATTR_DEPLOYMENT_ENVIRONMENT_NAME]: process.env.NODE_ENV || 'development'
    })
  ),
  instrumentations: [
    new HttpInstrumentation({
      ignoreIncomingRequestHook: (request) => request.url?.split('?', 1)[0] === '/actuator/health',
      redactedQueryParams: [
        'sig',
        'Signature',
        'AWSAccessKeyId',
        'X-Goog-Signature',
        'token',
        'access_token',
        'refresh_token',
        'code',
        'password',
        'keywords'
      ],
      applyCustomAttributesOnSpan: (span, request, response) => {
        const expressResponse = response as { locals?: Record<string, unknown> }
        const route = expressResponse.locals?.otelRoute
        if (typeof route !== 'string') return
        const httpRequest = request as { url?: string; path?: string }
        span.updateName(`${request.method || 'HTTP'} ${route}`)
        span.setAttribute(ATTR_HTTP_ROUTE, route)
        span.setAttribute(ATTR_URL_PATH, route)
        if ((httpRequest.url || httpRequest.path)?.includes('?')) {
          span.setAttribute(ATTR_URL_QUERY, 'REDACTED')
        }
      }
    }),
    new ExpressInstrumentation({
      ignoreLayersType: [ExpressLayerType.MIDDLEWARE]
    }),
    new RuntimeNodeInstrumentation({
      monitoringPrecision: 5000,
      captureUncaughtException: true
    })
  ]
})

sdk.start()

let shutdownPromise: Promise<void> | undefined

export const shutdownTelemetry = (): Promise<void> => {
  shutdownPromise ||= sdk.shutdown()
  return shutdownPromise
}
