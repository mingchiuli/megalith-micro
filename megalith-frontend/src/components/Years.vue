<script lang="ts" setup>
import { GET } from '@/http/http'
import { computed, ref } from 'vue'

const props = defineProps<{
  year: string
  yearDialogVisible: boolean
}>()

const emit = defineEmits<{
  (event: 'close'): void
  (event: 'update:year', payload: string): void
  (event: 'update:yearDialogVisible', payload: boolean): void
}>()

const years = ref<number[]>()

const yearDialogVisible = computed({
  get() {
    return props.yearDialogVisible
  },
  set(value) {
    emit('update:yearDialogVisible', value);
  }
})

let year = computed({
  get() {
    return props.year
  },
  set(value) {
    emit('update:year', value)
  }
})

const chooseYear = (y: number | string) => {
  year.value = y.toString()
  yearDialogVisible.value = false
  emit('close')
}

(async () => {
  years.value = await GET<number[]>('/public/blog/years')
})()

</script>

<template>
  <el-dialog v-model="yearDialogVisible" width="50%" append-to-body title="Archieve">
    <el-button type="primary" v-for="year in years" @click="chooseYear(year)">{{ year }}</el-button>
    <el-button type="primary" @click="chooseYear('')">清除</el-button>
  </el-dialog>
</template>

<style scoped>
.el-button {
  margin: 5px;
}
</style>