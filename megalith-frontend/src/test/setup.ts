import { config } from '@vue/test-utils'
import { beforeEach } from 'vitest'
import { createAppI18n } from '@/i18n'

const i18n = createAppI18n()
config.global.plugins = [i18n]

beforeEach(() => {
  i18n.global.locale.value = 'zh-CN'
})
