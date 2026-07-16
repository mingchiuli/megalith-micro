<script setup lang="ts">
import { pageStore } from '@/stores'
import { useI18n } from 'vue-i18n'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import en from 'element-plus/es/locale/lang/en'

const { locale, t } = useI18n({ useScope: 'global' })
const route = useRoute()
const elementLocale = computed(() => (locale.value === 'zh-CN' ? zhCn : en))

watchEffect(() => {
  const titleKey = route.meta.titleKey as string | undefined
  if (titleKey) {
    document.title = t(titleKey)
  } else if (route.meta.title) {
    document.title = route.meta.title as string
  }
})
</script>

<template>
  <el-config-provider :locale="elementLocale">
    <router-view />
    <MyFooterItem v-if="pageStore().front" />
    <LocaleSwitcher />
  </el-config-provider>
</template>
