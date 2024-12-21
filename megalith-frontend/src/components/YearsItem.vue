<script lang="ts" setup>
import { GET } from '@/http/http'
import { ref } from 'vue'

const emit = defineEmits<(event: 'close') => void>()
const years = ref<number[]>()
const year = defineModel('year')
const yearDialogVisible = defineModel<boolean>('yearDialogVisible')

const chooseYear = (y: number | string) => {
  year.value = y.toString()
  yearDialogVisible.value = false
  emit('close')
}

;(async () => {
  years.value = await GET<number[]>('/public/blog/years')
})()
</script>

<template>
  <el-dialog v-model="yearDialogVisible" title="Archive" width="600px">
    <el-button type="primary" v-for="year in years" v-bind:key="year" @click="chooseYear(year)">{{
      year
    }}</el-button>
  </el-dialog>
</template>

<style scoped>
.el-button {
  margin: 5px;
}
</style>
