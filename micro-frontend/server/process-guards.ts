import { logger } from './logger.js'

let installed = false

/**
 * Keeps the SSR process alive when a single request misbehaves. The server holds no
 * long-lived state, so logging and continuing is preferable to taking every page down
 * because one prefetch, stream, or socket rejected.
 */
export const installProcessGuards = (): void => {
  if (installed) return
  installed = true

  process.on('unhandledRejection', (reason) => {
    logger.error('Unhandled promise rejection', reason, { 'error.kind': 'unhandledRejection' })
  })

  process.on('uncaughtException', (error) => {
    logger.error('Uncaught exception', error, { 'error.kind': 'uncaughtException' })
  })
}
