import { createI18n } from 'vue-i18n'
import { messages } from './messages'

export type AppLocale = keyof typeof messages
export const DEFAULT_LOCALE: AppLocale = 'zh-CN'
const LOCALE_COOKIE = 'megalith_locale'
export const supportedLocales: AppLocale[] = ['zh-CN', 'en-US']

export const createAppI18n = (locale: AppLocale = DEFAULT_LOCALE) =>
  createI18n({
    legacy: false,
    globalInjection: true,
    locale,
    fallbackLocale: DEFAULT_LOCALE,
    messages
  })

export const persistLocale = (locale: AppLocale) => {
  if (typeof document === 'undefined') return
  document.documentElement.lang = locale
  document.cookie = `${LOCALE_COOKIE}=${locale}; Path=/; SameSite=Lax`
}
