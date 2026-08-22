import type { Attributes } from '@opentelemetry/api'
import { logs, SeverityNumber } from '@opentelemetry/api-logs'
import { serviceName, serviceVersion } from './telemetry.js'

type LogLevel = 'trace' | 'debug' | 'info' | 'warn' | 'error' | 'fatal'

type Logger = {
  trace: (message: string, attributes?: Attributes) => void
  debug: (message: string, attributes?: Attributes) => void
  info: (message: string, attributes?: Attributes) => void
  warn: (message: string, attributes?: Attributes) => void
  error: (message: string, error?: unknown, attributes?: Attributes) => void
  fatal: (message: string, error?: unknown, attributes?: Attributes) => void
}

const otelLogger = logs.getLogger(`${serviceName}.server`, serviceVersion)
const levels: LogLevel[] = ['trace', 'debug', 'info', 'warn', 'error', 'fatal']
const configuredLevel = process.env.LOG_LEVEL?.toLowerCase() || 'info'
const configuredThreshold = levels.indexOf(configuredLevel as LogLevel)
const threshold = configuredThreshold === -1 ? levels.indexOf('info') : configuredThreshold

const severityNumbers: Record<LogLevel, SeverityNumber> = {
  trace: SeverityNumber.TRACE,
  debug: SeverityNumber.DEBUG,
  info: SeverityNumber.INFO,
  warn: SeverityNumber.WARN,
  error: SeverityNumber.ERROR,
  fatal: SeverityNumber.FATAL
}

const consoleMethods: Record<LogLevel, (...data: unknown[]) => void> = {
  trace: console.debug,
  debug: console.debug,
  info: console.info,
  warn: console.warn,
  error: console.error,
  fatal: console.error
}

const emit = (level: LogLevel, message: string, attributes: Attributes = {}, error?: unknown) => {
  if (levels.indexOf(level) < threshold) return

  const record: Record<string, unknown> = {
    timestamp: new Date().toISOString(),
    severity: level.toUpperCase(),
    message,
    ...attributes
  }
  if (error instanceof Error) {
    record.error = {
      type: error.name,
      message: error.message,
      stack: error.stack
    }
  }

  consoleMethods[level](JSON.stringify(record))
  otelLogger.emit({
    severityNumber: severityNumbers[level],
    severityText: level.toUpperCase(),
    body: message,
    attributes,
    ...(error === undefined ? {} : { exception: error })
  })
}

export const logger: Logger = {
  trace: (message, attributes) => emit('trace', message, attributes),
  debug: (message, attributes) => emit('debug', message, attributes),
  info: (message, attributes) => emit('info', message, attributes),
  warn: (message, attributes) => emit('warn', message, attributes),
  error: (message, error, attributes) => emit('error', message, attributes, error),
  fatal: (message, error, attributes) => emit('fatal', message, attributes, error)
}
