<script setup lang="ts">
import { Switch } from '@element-plus/icons-vue'
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
const compactLocaleLabel = computed(() => (locale.value === 'zh-CN' ? '中' : 'EN'))
</script>

<template>
  <div class="locale-switcher">
    <select
      class="desktop-locale-select"
      v-model="selectedLocale"
      :aria-label="t('common.language')"
    >
      <option v-for="option in options" :key="option.value" :value="option.value">
        {{ option.label }}
      </option>
    </select>

    <label
      class="mobile-locale-control"
      :aria-label="t('common.language')"
      :title="t('common.language')"
    >
      <Switch class="mobile-locale-icon" aria-hidden="true" />
      <span>{{ compactLocaleLabel }}</span>
      <select
        class="mobile-locale-select"
        v-model="selectedLocale"
        :aria-label="t('common.language')"
      >
        <option v-for="option in options" :key="option.value" :value="option.value">
          {{ option.label }}
        </option>
      </select>
    </label>
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

.desktop-locale-select {
  width: 96px;
  height: 32px;
  padding: 0 8px;
  color: var(--el-text-color-regular);
  background: var(--el-bg-color-overlay);
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  font: inherit;
}

.desktop-locale-select:focus-visible {
  outline: 2px solid var(--el-color-primary);
  outline-offset: 2px;
}

.mobile-locale-control {
  display: none;
}

.mobile-locale-control {
  position: relative;
  align-items: center;
  justify-content: center;
  gap: 5px;
  width: 58px;
  height: 44px;
  padding: 0;
  color: var(--el-text-color-primary);
  background: var(--el-bg-color-overlay);
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  font: inherit;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
}

.mobile-locale-control:focus-within {
  outline: 2px solid var(--el-color-primary);
  outline-offset: 2px;
}

.mobile-locale-icon {
  width: 20px;
  height: 20px;
}

.mobile-locale-select {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  cursor: pointer;
}

@media (max-width: 600px) {
  .locale-switcher {
    right: 12px;
    bottom: 72px;
    width: 58px;
  }

  .desktop-locale-select {
    display: none;
  }

  .mobile-locale-control {
    display: inline-flex;
  }
}
</style>
