const TRACE_VERSION = '00'
const TRACE_FLAG = '01'

function randomHex(length: number): string {
  const bytes = crypto.getRandomValues(new Uint8Array(length / 2))
  return Array.from(bytes).map(b => b.toString(16).padStart(2, '0')).join('')
}

// Root trace ID generated once per page session
const ROOT_TRACE_ID = randomHex(32)

export function createTraceParent(spanId: string): string {
  return `${TRACE_VERSION}-${ROOT_TRACE_ID}-${spanId}-${TRACE_FLAG}`
}

export function generateSpanId(): string {
  return randomHex(16)
}
