/**
 * Development-only logging utility
 * Prevents console statements from appearing in production builds
 */

const isDev = import.meta.env.DEV

export const logger = {
  log: (...args: unknown[]): void => {
    if (isDev) console.log(...args)
  },
  warn: (...args: unknown[]): void => {
    if (isDev) console.warn(...args)
  },
  error: (...args: unknown[]): void => {
    // Always log errors, even in production
    console.error(...args)
  },
  debug: (...args: unknown[]): void => {
    if (isDev) console.debug(...args)
  },
  info: (...args: unknown[]): void => {
    if (isDev) console.info(...args)
  }
}