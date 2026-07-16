<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { setLocale, type AppLocale } from '@/i18n'

const { locale, t } = useI18n({ useScope: 'global' })
const selectedLocale = computed({
  get: () => locale.value as AppLocale,
  set: (value: AppLocale) => setLocale(value)
})
const options = computed(() => [
  { value: 'zh-CN' as const, label: t('common.chinese') },
  { value: 'en-US' as const, label: t('common.english') }
])
</script>

<template>
  <div class="locale-switcher">
    <el-select
      v-model="selectedLocale"
      :aria-label="t('common.language')"
      size="small"
      placement="top"
    >
      <el-option
        v-for="option in options"
        :key="option.value"
        :label="option.label"
        :value="option.value"
      />
    </el-select>
  </div>
</template>

<style scoped>
.locale-switcher {
  position: fixed;
  right: 16px;
  bottom: 16px;
  z-index: 2100;
  width: 96px;
}

@media (max-width: 600px) {
  .locale-switcher {
    right: 12px;
    bottom: 72px;
  }
}
</style>
