<script lang="ts" setup>
import { ref, computed, type WritableComputedRef } from 'vue'
import axios from '@/axios'
import type { Data } from '@/type/entity'
import type { AxiosResponse } from 'axios'
import router from '@/router'

const emit = defineEmits<(event: 'update:readTokenDialog', payload: boolean) => void>()

const props = defineProps<{
  readTokenDialog: boolean
  blogId: number
}>()

let visible: WritableComputedRef<boolean> = computed({
  get() {
    return props.readTokenDialog
  },
  set(value: boolean) {
    emit('update:readTokenDialog', value);
  },
})

const input = ref('')

const submit = async () => {
  const resp: AxiosResponse<Data<boolean>> = await axios.get(`/public/blog/token/${props.blogId}?readToken=${input.value}`);
  if (resp.data.data) {
    router.push({
      name: 'blog',
      params: {
        id: props.blogId,
        token: input.value
      }
    })
  } else {
    //@ts-ignore  
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