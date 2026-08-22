import assert from 'node:assert/strict'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

type OtelSignal = 'traces' | 'metrics' | 'logs'

const root = path.resolve(fileURLToPath(new URL('..', import.meta.url)))
const binary = path.join(root, 'dist/bin/megalith-frontend')

const waitFor = async (
  condition: () => boolean | Promise<boolean>,
  timeoutMillis: number,
  message: string
): Promise<void> => {
  const deadline = Date.now() + timeoutMillis
  while (Date.now() < deadline) {
    if (await condition()) return
    await Bun.sleep(50)
  }
  assert.fail(message)
}

const reservePort = async (): Promise<number> => {
  const reservation = Bun.serve({ port: 0, fetch: () => new Response() })
  const port = reservation.port
  await reservation.stop(true)
  if (port === undefined) throw new Error('Bun did not assign a reservation port')
  return port
}

const signals = new Set<OtelSignal>()
const payloads = new Map<OtelSignal, Buffer[]>()
const collector = Bun.serve({
  hostname: '127.0.0.1',
  port: 0,
  async fetch(request) {
    const signal = new URL(request.url).pathname.match(/^\/v1\/(traces|metrics|logs)$/)?.[1] as
      OtelSignal | undefined
    if (request.method !== 'POST' || !signal) return new Response(null, { status: 404 })

    signals.add(signal)
    const signalPayloads = payloads.get(signal) || []
    signalPayloads.push(Buffer.from(await request.arrayBuffer()))
    payloads.set(signal, signalPayloads)
    return new Response(null, {
      status: 200,
      headers: { 'Content-Type': 'application/x-protobuf' }
    })
  }
})

const propagatedTraceparents: string[] = []
const propagatedTracestates: string[] = []
const propagatedBaggage: string[] = []
const gateway = Bun.serve({
  hostname: '127.0.0.1',
  port: 0,
  fetch(request) {
    const traceparent = request.headers.get('traceparent')
    if (traceparent) propagatedTraceparents.push(traceparent)
    const tracestate = request.headers.get('tracestate')
    if (tracestate) propagatedTracestates.push(tracestate)
    const baggage = request.headers.get('baggage')
    if (baggage) propagatedBaggage.push(baggage)

    if (new URL(request.url).pathname === '/public/blog/stat') {
      return Response.json({
        msg: 'OK',
        data: { dayVisit: 1, weekVisit: 2, monthVisit: 3, yearVisit: 4 }
      })
    }
    return Response.json({ msg: 'Not Found', data: null }, { status: 404 })
  }
})

const frontendPort = await reservePort()
const traceId = '0123456789abcdef0123456789abcdef'
const parentSpanId = '0123456789abcdef'
const child = Bun.spawn([binary], {
  cwd: root,
  env: {
    ...process.env,
    NODE_ENV: 'production',
    PORT: String(frontendPort),
    SSR_API_BASE_URL: `http://127.0.0.1:${gateway.port}`,
    APP_ORIGIN: 'https://chiu.wiki',
    OTEL_SERVICE_VERSION: 'otel-smoke-version',
    OTEL_EXPORTER_OTLP_ENDPOINT: `http://127.0.0.1:${collector.port}`,
    OTEL_TRACES_SAMPLER: 'parentbased_traceidratio',
    OTEL_TRACES_SAMPLER_ARG: '0.5',
    OTEL_BSP_SCHEDULE_DELAY: '60000',
    OTEL_BLRP_SCHEDULE_DELAY: '60000',
    OTEL_METRIC_EXPORT_INTERVAL: '200',
    OTEL_METRIC_EXPORT_TIMEOUT: '100'
  },
  stdin: 'ignore',
  stdout: 'pipe',
  stderr: 'pipe'
})
const stdout = new Response(child.stdout).text()
const stderr = new Response(child.stderr).text()
const healthUrl = `http://127.0.0.1:${frontendPort}/actuator/health`

try {
  await waitFor(
    async () => {
      try {
        return (await fetch(healthUrl)).ok
      } catch {
        return false
      }
    },
    10_000,
    'Standalone frontend did not start'
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
    'Bun did not propagate trace context to the gateway'
  )
  for (const traceparent of propagatedTraceparents) {
    assert.match(traceparent, new RegExp(`^00-${traceId}-[\\da-f]{16}-01$`))
    assert.notEqual(traceparent.split('-')[2], parentSpanId)
  }
  assert.ok(propagatedTracestates.includes('vendor=value'))
  assert.ok(propagatedBaggage.includes('tenant.id=acme'))

  await waitFor(() => signals.has('metrics'), 10_000, 'Bun runtime metrics were not exported')

  child.kill('SIGTERM')
  assert.equal(await child.exited, 0)
  const output = `${await stdout}${await stderr}`
  assert.deepEqual(signals, new Set<OtelSignal>(['metrics', 'traces', 'logs']), output)

  for (const signal of signals) {
    const payload = Buffer.concat(payloads.get(signal) || []).toString('utf8')
    assert.match(payload, /megalith-frontend/)
    assert.match(payload, /otel-smoke-version/)
  }
  const tracePayload = Buffer.concat(payloads.get('traces') || []).toString('utf8')
  assert.match(tracePayload, /GET \/|ssr\.render/)
  assert.doesNotMatch(tracePayload, new RegExp(sensitiveQueryValue))

  const metricPayload = Buffer.concat(payloads.get('metrics') || []).toString('utf8')
  assert.match(metricPayload, /bun\.runtime\.memory\.rss/)
  assert.match(metricPayload, /bun\.runtime\.jsc\.heap\.size/)
  assert.match(metricPayload, /bun\.runtime\.event_loop\.delay\.p99/)
  assert.doesNotMatch(metricPayload, /nodejs\.eventloop\.utilization/)
  assert.doesNotMatch(metricPayload, /v8js\.gc\.duration/)

  const logPayload = Buffer.concat(payloads.get('logs') || []).toString('utf8')
  assert.match(logPayload, /HTTP server stopped/)
  console.log('Bun HTTP spans, runtime metrics, propagation, redaction, and shutdown verified')
} finally {
  if (child.exitCode === null) {
    child.kill('SIGKILL')
    await child.exited
  }
  await Promise.all([collector.stop(true), gateway.stop(true)])
}
