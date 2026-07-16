import { describe, expect, it } from 'vitest'
import { i18n, setLocale } from '@/i18n'

describe('i18n', () => {
  it('switches between Chinese and English and persists the selection', () => {
    setLocale('en-US')

    expect(i18n.global.t('common.confirm')).toBe('Confirm')
    expect(document.documentElement.lang).toBe('en-US')
    expect(localStorage.getItem('megalith-locale')).toBe('en-US')

    setLocale('zh-CN')

    expect(i18n.global.t('common.confirm')).toBe('确定')
    expect(document.documentElement.lang).toBe('zh-CN')
  })
})
