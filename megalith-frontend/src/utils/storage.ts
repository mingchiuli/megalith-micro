/**
 * Centralized localStorage management
 * Provides type-safe access and consistent error handling
 */

export const STORAGE_KEYS = {
  ACCESS_TOKEN: 'accessToken',
  USER_INFO: 'userinfo'
} as const

type StorageKey = typeof STORAGE_KEYS[keyof typeof STORAGE_KEYS]

const LEGACY_REFRESH_TOKEN_KEY = 'refreshToken'
localStorage.removeItem(LEGACY_REFRESH_TOKEN_KEY)

/**
 * Get access token
 */
export const getAccessToken = (): string | null => {
  return localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN)
}

/**
 * Set access token
 */
export const setAccessToken = (token: string): void => {
  localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, token)
}

/**
 * Get user info - parsed as JSON
 */
export const getUserInfo = <T>(): T | null => {
  const info = localStorage.getItem(STORAGE_KEYS.USER_INFO)
  if (!info) return null
  try {
    return JSON.parse(info) as T
  } catch {
    return null
  }
}

/**
 * Get user info as raw string
 */
export const getUserInfoRaw = (): string | null => {
  return localStorage.getItem(STORAGE_KEYS.USER_INFO)
}

/**
 * Set user info
 */
export const setUserInfo = (info: unknown): void => {
  localStorage.setItem(STORAGE_KEYS.USER_INFO, JSON.stringify(info))
}

/**
 * Check if user is logged in (has access token)
 */
export const isLoggedIn = (): boolean => {
  return !!localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN)
}

/**
 * Clear auth-related storage items
 */
export const clearAuth = (): void => {
  localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN)
  localStorage.removeItem(LEGACY_REFRESH_TOKEN_KEY)
  localStorage.removeItem(STORAGE_KEYS.USER_INFO)
}

/**
 * Clear all storage
 */
export const clearAll = (): void => {
  localStorage.clear()
}

/**
 * Generic get method
 */
export const get = (key: StorageKey | string): string | null => {
  return localStorage.getItem(key)
}

/**
 * Generic set method
 */
export const set = (key: StorageKey | string, value: string): void => {
  localStorage.setItem(key, value)
}

/**
 * Generic remove method
 */
export const remove = (key: StorageKey | string): void => {
  localStorage.removeItem(key)
}

/**
 * Storage object for convenience (backward compatibility)
 */
export const storage = {
  getAccessToken,
  setAccessToken,
  getUserInfo,
  getUserInfoRaw,
  setUserInfo,
  isLoggedIn,
  clearAuth,
  clearAll,
  get,
  set,
  remove
}
