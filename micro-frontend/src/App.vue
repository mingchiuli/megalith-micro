<script setup lang="ts">
import { loginStateStore, themeStore } from '@/stores'
import { useI18n } from 'vue-i18n'
import { useHead } from '@unhead/vue'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import en from 'element-plus/es/locale/lang/en'

const { locale, t } = useI18n({ useScope: 'global' })
const route = useRoute()
const router = useRouter()
const elementLocale = computed(() => (locale.value === 'zh-CN' ? zhCn : en))
const { isDark } = storeToRefs(themeStore())
const loginState = loginStateStore()
const { sessionExpired } = storeToRefs(loginState)
const showGlobalFooter = computed(() => route.meta.layout !== 'system')

const continueToLogin = async () => {
  const redirect = loginState.sessionExpiredRedirect
  sessionExpired.value = false
  await router.push({ name: 'login', query: redirect ? { redirect } : undefined })
}

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
    <el-dialog
      :model-value="sessionExpired"
      :title="t('auth.sessionExpiredTitle')"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
      width="420"
    >
      <span>{{ t('auth.sessionExpiredMessage') }}</span>
      <template #footer>
        <el-button type="primary" @click="continueToLogin">
          {{ t('auth.loginAgain') }}
        </el-button>
      </template>
    </el-dialog>
  </el-config-provider>
</template>
