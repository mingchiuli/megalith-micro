<script setup lang="ts">
import { themeStore } from '@/stores'
import { useI18n } from 'vue-i18n'
import { useHead } from '@unhead/vue'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import en from 'element-plus/es/locale/lang/en'

const { locale, t } = useI18n({ useScope: 'global' })
const route = useRoute()
const elementLocale = computed(() => (locale.value === 'zh-CN' ? zhCn : en))
const { isDark } = storeToRefs(themeStore())
const showGlobalFooter = computed(() => route.meta.layout !== 'system')

useHead(() => {
  const titleKey = route.meta.titleKey as string | undefined
  const htmlAttrs = { lang: locale.value, class: isDark.value ? 'dark' : undefined }
  if (titleKey) {
    return { title: t(titleKey), htmlAttrs }
  }
  return { title: route.meta.title as string | undefined, htmlAttrs }
})
</script>

<template>
  <el-config-provider :locale="elementLocale">
    <router-view />
    <MyFooterItem v-if="showGlobalFooter" />
    <LocaleSwitcher />
  </el-config-provider>
</template>
