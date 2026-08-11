import assert from 'node:assert/strict'
import { spawn } from 'node:child_process'
import { once } from 'node:events'
import { createServer } from 'node:http'
import type { Server } from 'node:http'

type OtelSignal = 'traces' | 'metrics' | 'logs'

const listen = async (server: Server): Promise<number> => {
  server.listen(0, '127.0.0.1')
  await once(server, 'listening')
  const address = server.address()
  assert.ok(address && typeof address === 'object')
  return address.port
}

const close = (server: Server): Promise<void> =>
  new Promise((resolve, reject) => server.close((error) => (error ? reject(error) : resolve())))

const waitFor = async (
  condition: () => boolean,
  timeoutMillis: number,
  message: string
): Promise<void> => {
  const deadline = Date.now() + timeoutMillis
  while (Date.now() < deadline) {
    if (condition()) return
    await new Promise((resolve) => setTimeout(resolve, 50))
  }
  assert.fail(message)
}

const reservePort = async (): Promise<number> => {
  const server = createServer()
  const port = await listen(server)
  await close(server)
  return port
}

const signals = new Set<OtelSignal>()
const payloads = new Map<OtelSignal, Buffer[]>()
const collector = createServer((request, response) => {
  const signal = request.url?.match(/^\/v1\/(traces|metrics|logs)$/)?.[1] as OtelSignal | undefined
  const chunks: Buffer[] = []
  request.on('data', (chunk: Buffer | string) => chunks.push(Buffer.from(chunk)))
  request.on('end', () => {
    if (request.method === 'POST' && signal) {
      signals.add(signal)
      const signalPayloads = payloads.get(signal) || []
      signalPayloads.push(Buffer.concat(chunks))
      payloads.set(signal, signalPayloads)
      response.statusCode = 200
      response.setHeader('Content-Type', 'application/x-protobuf')
      response.end()
      return
    }
    response.statusCode = 404
    response.end()
  })
})

const propagatedTraceparents: string[] = []
const propagatedTracestates: string[] = []
const propagatedBaggage: string[] = []
const gateway = createServer((request, response) => {
  const traceparent = request.headers.traceparent
  if (typeof traceparent === 'string') propagatedTraceparents.push(traceparent)
  const tracestate = request.headers.tracestate
  if (typeof tracestate === 'string') propagatedTracestates.push(tracestate)
  const baggage = request.headers.baggage
  if (typeof baggage === 'string') propagatedBaggage.push(baggage)
  response.setHeader('Content-Type', 'application/json')
  if (request.url === '/public/blog/stat') {
    response.end(
      JSON.stringify({
        msg: 'OK',
        data: { dayVisit: 1, weekVisit: 2, monthVisit: 3, yearVisit: 4 }
      })
    )
    return
  }
  response.statusCode = 404
  response.end(JSON.stringify({ msg: 'Not Found', data: null }))
})

const collectorPort = await listen(collector)
const gatewayPort = await listen(gateway)
const frontendPort = await reservePort()
const traceId = '0123456789abcdef0123456789abcdef'
const parentSpanId = '0123456789abcdef'
const child = spawn(
  process.execPath,
  [
    '--import',
    './dist/node/server/register-hooks.js',
    '--import',
    './dist/node/server/telemetry.js',
    './dist/node/server/index.js'
  ],
  {
    cwd: new URL('../../..', import.meta.url),
    env: {
      ...process.env,
      NODE_ENV: 'production',
      PORT: String(frontendPort),
      SSR_API_BASE_URL: `http://127.0.0.1:${gatewayPort}`,
      APP_ORIGIN: 'https://chiu.wiki',
      OTEL_SERVICE_VERSION: 'otel-smoke-version',
      OTEL_EXPORTER_OTLP_ENDPOINT: `http://127.0.0.1:${collectorPort}`,
      OTEL_TRACES_SAMPLER: 'parentbased_traceidratio',
      OTEL_TRACES_SAMPLER_ARG: '0.5',
      OTEL_BSP_SCHEDULE_DELAY: '60000',
      OTEL_BLRP_SCHEDULE_DELAY: '60000',
      OTEL_METRIC_EXPORT_INTERVAL: '200',
      OTEL_METRIC_EXPORT_TIMEOUT: '100'
    },
    stdio: ['ignore', 'pipe', 'pipe']
  }
)

let output = ''
child.stdout.setEncoding('utf8')
child.stderr.setEncoding('utf8')
child.stdout.on('data', (chunk: string) => {
  output += chunk
})
child.stderr.on('data', (chunk: string) => {
  output += chunk
})

try {
  await waitFor(
    () => output.includes('Megalith SSR server started'),
    10_000,
    `Frontend did not start:\n${output}`
  )

  const sensitiveQueryValue = 'otel-smoke-secret'
  const response = await fetch(
    `http://127.0.0.1:${frontendPort}/?token=${sensitiveQueryValue}&view=home`,
    {
      headers: {
        traceparent: `00-${traceId}-${parentSpanId}-01`,
        tracestate: 'vendor=value',
        baggage: 'tenant.id=acme'
      }
    }
  )
  assert.equal(response.status, 200)
  assert.match(await response.text(), /class="intro-notebook-icon"/)

  await waitFor(
    () => propagatedTraceparents.length > 0,
    5_000,
    'Node did not propagate trace context to the gateway'
  )
  for (const traceparent of propagatedTraceparents) {
    assert.match(traceparent, new RegExp(`^00-${traceId}-[\\da-f]{16}-01$`))
    assert.notEqual(traceparent.split('-')[2], parentSpanId)
  }
  assert.ok(propagatedTracestates.includes('vendor=value'))
  assert.ok(propagatedBaggage.includes('tenant.id=acme'))

  await waitFor(() => signals.has('metrics'), 10_000, 'Runtime metrics were not exported')

  child.kill('SIGTERM')
  const [code, exitSignal] = await once(child, 'exit')
  assert.equal(exitSignal, null)
  assert.equal(code, 0, output)
  assert.deepEqual(signals, new Set<OtelSignal>(['metrics', 'traces', 'logs']))

  for (const signal of signals) {
    const payload = Buffer.concat(payloads.get(signal) || []).toString('utf8')
    assert.match(payload, /megalith-frontend/)
    assert.match(payload, /otel-smoke-version/)
  }
  const tracePayload = Buffer.concat(payloads.get('traces') || []).toString('utf8')
  assert.doesNotMatch(tracePayload, new RegExp(sensitiveQueryValue))
  const logPayload = Buffer.concat(payloads.get('logs') || []).toString('utf8')
  assert.match(logPayload, /HTTP server stopped/)
  console.log('OTel signals, resources, propagation, redaction, and shutdown verified')
} finally {
  if (child.exitCode === null && child.signalCode === null) {
    child.kill('SIGKILL')
    await once(child, 'exit')
  }
  await Promise.all([close(collector), close(gateway)])
}
