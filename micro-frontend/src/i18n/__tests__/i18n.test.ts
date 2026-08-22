import { describe, expect, it } from 'vitest'
import { createAppI18n, persistLocale, resolveAppLocale } from '@/i18n'

describe('i18n', () => {
  it('resolves the SSR document locale before browser language fallbacks', () => {
    expect(resolveAppLocale('en-US', 'zh-CN')).toBe('en-US')
    expect(resolveAppLocale('', 'en-GB')).toBe('en-US')
    expect(resolveAppLocale('invalid', 'zh-CN')).toBe('zh-CN')
    expect(resolveAppLocale()).toBe('zh-CN')
  })

  it('switches between Chinese and English and persists the selection', () => {
    const i18n = createAppI18n()
    i18n.global.locale.value = 'en-US'
    persistLocale('en-US')

    expect(i18n.global.t('common.confirm')).toBe('Confirm')
    expect(document.documentElement.lang).toBe('en-US')
    expect(document.cookie).toContain('megalith_locale=en-US')

    i18n.global.locale.value = 'zh-CN'
    persistLocale('zh-CN')

    expect(i18n.global.t('common.confirm')).toBe('确定')
    expect(document.documentElement.lang).toBe('zh-CN')
  })
})
