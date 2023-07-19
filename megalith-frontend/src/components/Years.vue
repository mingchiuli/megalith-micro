<script lang="ts" setup>
import axios from '@/axios'
import { computed, ref, type Ref, type WritableComputedRef } from 'vue'
import type { AxiosResponse } from 'axios'
import type { Data } from '@/type/entity'

const props = defineProps<{
  year: string
}>()

const emit = defineEmits<{
  (event: 'close'): void
  (event: 'update:year', payload: string): void
}>()

let years: Ref<number[]> = ref([])

let year: WritableComputedRef<string> = computed({
  get() {
    return props.year
  },
  set(value: string) {
    emit('update:year', value);
  }
})

const chooseYear = (y: number | string) => {
  year.value = y.toString()
  emit("close")
}

axios.get('/public/blog/years')
  .then((resp: AxiosResponse<Data<number[]>>) => {
    years.value = resp.data.data
  })
</script>

<template>
  <el-button type="primary" v-for="year in years" @click="chooseYear(year)">{{ year }}</el-button>
  <el-button type="primary" @click="chooseYear('')">清除</el-button>
</template>

<style scoped>
.el-button {
  margin: 5px;
}
</style>