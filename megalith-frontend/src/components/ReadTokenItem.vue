<script lang="ts" setup>
import { ref } from 'vue'
import { GET } from '@/http/http'
import router from '@/router'

const { blogId } = defineProps<{
  blogId: number
}>()

const readTokenDialogVisible = defineModel<boolean>('readTokenDialogVisible')
const input = ref<string>()

const submit = async () => {
  const valid = await GET<boolean>(`/public/blog/token/${blogId}?readToken=${input.value}`)
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
    ElMessage.error('token error')
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
    title="阅读码"
    width="300px"
    :before-close="handleClose"
  >
    <el-input v-model="input" type="password" placeholder="Please input password" show-password />
    <template #footer>
      <span class="dialog-footer">
        <el-button type="primary" @click="submit"> 提交 </el-button>
      </span>
    </template>
  </el-dialog>
</template>
