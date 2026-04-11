import { WebTracerProvider } from '@opentelemetry/sdk-trace-web'
import { OTLPTraceExporter } from '@opentelemetry/exporter-trace-otlp-http'
import { W3CTraceContextPropagator } from '@opentelemetry/core'
import { resourceFromAttributes } from '@opentelemetry/resources'
import { ATTR_SERVICE_NAME } from '@opentelemetry/semantic-conventions'
import { BatchSpanProcessor } from '@opentelemetry/sdk-trace-web'

const resource = resourceFromAttributes({
  [ATTR_SERVICE_NAME]: 'megalith-frontend',
})

const provider = new WebTracerProvider({
  resource,
  spanProcessors: [new BatchSpanProcessor(new OTLPTraceExporter())],
})

provider.register({
  propagator: new W3CTraceContextPropagator(),
})
