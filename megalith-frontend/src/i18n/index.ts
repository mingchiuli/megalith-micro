import { createI18n } from 'vue-i18n'
import dayjs from 'dayjs'
import 'dayjs/locale/en'
import 'dayjs/locale/zh-cn'
import { messages } from './messages'

export type AppLocale = keyof typeof messages

const STORAGE_KEY = 'megalith-locale'
const supportedLocales: AppLocale[] = ['zh-CN', 'en-US']

const getInitialLocale = (): AppLocale => {
  try {
    const saved = localStorage.getItem(STORAGE_KEY) as AppLocale | null
    if (saved && supportedLocales.includes(saved)) return saved
  } catch {
    // Storage may be unavailable in privacy-restricted contexts.
  }
  return navigator.language.toLowerCase().startsWith('zh') ? 'zh-CN' : 'en-US'
}

const initialLocale = getInitialLocale()

export const i18n = createI18n({
  legacy: false,
  globalInjection: true,
  locale: initialLocale,
  fallbackLocale: 'zh-CN',
  messages
})

const applyLocale = (locale: AppLocale) => {
  document.documentElement.lang = locale
  dayjs.locale(locale === 'zh-CN' ? 'zh-cn' : 'en')
}

export const setLocale = (locale: AppLocale) => {
  i18n.global.locale.value = locale
  applyLocale(locale)
  try {
    localStorage.setItem(STORAGE_KEY, locale)
  } catch {
    // Keep the in-memory locale when persistence is unavailable.
  }
}

applyLocale(initialLocale)
