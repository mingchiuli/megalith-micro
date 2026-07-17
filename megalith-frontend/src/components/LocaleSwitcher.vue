<script setup lang="ts">
import { Check, Switch } from '@element-plus/icons-vue'
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

const handleLocaleCommand = (value: AppLocale) => {
  setLocale(value)
}
</script>

<template>
  <div class="locale-switcher">
    <el-select
      class="desktop-locale-select"
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

    <el-dropdown
      class="mobile-locale-dropdown"
      trigger="click"
      placement="top-end"
      @command="handleLocaleCommand"
    >
      <button
        class="mobile-locale-trigger"
        type="button"
        :aria-label="t('common.language')"
        :title="t('common.language')"
      >
        <el-icon :size="20"><Switch /></el-icon>
        <span>{{ compactLocaleLabel }}</span>
      </button>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item
            v-for="option in options"
            :key="option.value"
            :command="option.value"
            :disabled="option.value === selectedLocale"
          >
            <span class="locale-option-label">{{ option.label }}</span>
            <el-icon v-if="option.value === selectedLocale"><Check /></el-icon>
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
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

.mobile-locale-dropdown {
  display: none;
}

.mobile-locale-trigger {
  display: inline-flex;
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

.mobile-locale-trigger:focus-visible {
  outline: 2px solid var(--el-color-primary);
  outline-offset: 2px;
}

.locale-option-label {
  min-width: 64px;
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

  .mobile-locale-dropdown {
    display: inline-flex;
  }
}
</style>
