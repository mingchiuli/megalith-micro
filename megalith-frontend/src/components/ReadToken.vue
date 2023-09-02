<script lang="ts" setup>
import { ref, computed } from 'vue'
import { GET } from '@/http/http'
import router from '@/router'

const emit = defineEmits<(event: 'update:readTokenDialog', payload: boolean) => void>()

const props = defineProps<{
  readTokenDialog: boolean
  blogId: number
}>()

let visible = computed({
  get() {
    return props.readTokenDialog
  },
  set(value) {
    emit('update:readTokenDialog', value);
  },
})

const input = ref('')

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
  emit('update:readTokenDialog', false);
};


const handleClose = () => {
  emit('update:readTokenDialog', false)
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