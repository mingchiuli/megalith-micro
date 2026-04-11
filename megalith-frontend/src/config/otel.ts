const TRACE_VERSION = '00'
const TRACE_FLAG = '01'

function randomHex(length: number): string {
  const bytes = crypto.getRandomValues(new Uint8Array(length / 2))
  return Array.from(bytes).map(b => b.toString(16).padStart(2, '0')).join('')
}

export function createTraceParent(): string {
  return `${TRACE_VERSION}-${randomHex(32)}-${randomHex(16)}-${TRACE_FLAG}`
}
