<script lang="ts" setup>
import { ref, computed } from 'vue'
import { GET } from '@/http/http'
import router from '@/router'

const emit = defineEmits<(event: 'update:readTokenDialogVisible', payload: boolean) => void>()

const props = defineProps<{
  readTokenDialogVisible: boolean
  blogId: number
}>()

let visible = computed({
  get() {
    return props.readTokenDialogVisible
  },
  set(value) {
    emit('update:readTokenDialogVisible', value);
  },
})

const input = ref<string>()

const submit = async () => {
  const valid = await GET<boolean>(`/public/blog/token/${props.blogId}?readToken=${input.value}`)
  if (valid) {
    router.push({
      name: 'blog',
      params: {
        id: props.blogId
      },
      query: {
        token: input.value
      }
    })
  } else {
    ElMessage.error("token error")
  }
  visible.value = false
}

const handleClose = () => {
  visible.value = false
}
</script>

<template>
  <el-dialog v-model="visible" title="阅读码" width="300px" :before-close="handleClose">
    <el-input v-model="input" type="password" placeholder="Please input password" show-password />
    <template #footer>
      <span class="dialog-footer">
        <el-button type="primary" @click="submit">
          提交
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>