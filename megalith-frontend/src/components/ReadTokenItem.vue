<script lang="ts" setup>
import { useHttp } from '@/http/http'
import { API_ENDPOINTS } from '@/config/apiConfig'
import { useI18n } from 'vue-i18n'
import type { BlogExhibit } from '@/type/entity'
import { protectedBlogStore } from '@/stores'

const { t } = useI18n()
const { POST } = useHttp()
const router = useRouter()

const { blogId } = defineProps<{
  blogId: number
}>()

const readTokenDialogVisible = defineModel<boolean>('readTokenDialogVisible')
const input = ref<string>()
const submitting = ref(false)

const submit = async () => {
  if (!input.value?.trim()) return
  submitting.value = true
  try {
    const blog = await POST<BlogExhibit>(API_ENDPOINTS.BLOG_PUBLIC.READ_SECRET_BLOG(blogId), {
      readToken: input.value
    })
    protectedBlogStore().put(blogId, blog)
    input.value = ''
    readTokenDialogVisible.value = false
    await router.push({
      name: 'blog',
      params: {
        id: blogId
      }
    })
  } finally {
    submitting.value = false
  }
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
        <el-button type="primary" :loading="submitting" :disabled="submitting" @click="submit">{{
          t('common.submit')
        }}</el-button>
      </span>
    </template>
  </el-dialog>
</template>
