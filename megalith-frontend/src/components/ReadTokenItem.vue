<script lang="ts" setup>
import { GET } from '@/http/http'
import router from '@/router'
import { API_ENDPOINTS, buildQueryUrl } from '@/config/apiConfig'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const { blogId } = defineProps<{
  blogId: number
}>()

const readTokenDialogVisible = defineModel<boolean>('readTokenDialogVisible')
const input = ref<string>()

const submit = async () => {
  const valid = await GET<boolean>(
    buildQueryUrl(API_ENDPOINTS.BLOG_PUBLIC.VALIDATE_READ_TOKEN(blogId), {
      readToken: input.value
    })
  )
  if (valid) {
    router.push({
      name: 'blog',
      params: {
        id: blogId
      },
      query: {
        token: input.value
      }
    })
  } else {
    ElMessage.error(t('auth.readCodeError'))
  }
  readTokenDialogVisible.value = false
}

const handleClose = () => {
  input.value = ''
  readTokenDialogVisible.value = false
}
</script>

<template>
  <el-dialog
    v-model="readTokenDialogVisible"
    :title="t('auth.readCode')"
    width="300px"
    :before-close="handleClose"
  >
    <el-input
      v-model="input"
      type="password"
      :placeholder="t('auth.readCodePlaceholder')"
      show-password
    />
    <template #footer>
      <span class="dialog-footer">
        <el-button type="primary" @click="submit">{{ t('common.submit') }}</el-button>
      </span>
    </template>
  </el-dialog>
</template>
