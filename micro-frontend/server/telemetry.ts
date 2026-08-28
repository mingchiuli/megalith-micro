import { logs, SeverityNumber } from '@opentelemetry/api-logs'
import { HttpInstrumentation } from '@opentelemetry/instrumentation-http'
import { defaultResource, resourceFromAttributes } from '@opentelemetry/resources'
import { NodeSDK } from '@opentelemetry/sdk-node'
import {
  ATTR_DEPLOYMENT_ENVIRONMENT_NAME,
  ATTR_SERVICE_NAME,
  ATTR_SERVICE_NAMESPACE,
  ATTR_SERVICE_VERSION
} from '@opentelemetry/semantic-conventions'
import packageJson from '../package.json' with { type: 'json' }
import { registerBunRuntimeMetrics } from './runtime-metrics.js'

process.env.OTEL_EXPORTER_OTLP_ENDPOINT ||= 'http://127.0.0.1:8200'
process.env.OTEL_EXPORTER_OTLP_PROTOCOL ||= 'http/protobuf'
process.env.OTEL_TRACES_SAMPLER ||= 'parentbased_traceidratio'
process.env.OTEL_TRACES_SAMPLER_ARG ||= '0.05'

export const serviceName = process.env.OTEL_SERVICE_NAME || 'megalith-frontend'
export const serviceVersion = process.env.OTEL_SERVICE_VERSION || packageJson.version

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
      disableIncomingRequestInstrumentation: true,
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
      ]
    })
  ]
})

sdk.start()
registerBunRuntimeMetrics(serviceName, serviceVersion)

const runtimeLogger = logs.getLogger(`${serviceName}.runtime`, serviceVersion)
process.on('uncaughtExceptionMonitor', (error, origin) => {
  const attributes = { 'exception.origin': origin }
  console.error(
    JSON.stringify({
      timestamp: new Date().toISOString(),
      severity: 'FATAL',
      message: 'Uncaught exception',
      ...attributes,
      error: { type: error.name, message: error.message, stack: error.stack }
    })
  )
  runtimeLogger.emit({
    severityNumber: SeverityNumber.FATAL,
    severityText: 'FATAL',
    body: 'Uncaught exception',
    attributes,
    exception: error
  })
})

let shutdownPromise: Promise<void> | undefined

export const shutdownTelemetry = (): Promise<void> => {
  shutdownPromise ||= sdk.shutdown()
  return shutdownPromise
}
