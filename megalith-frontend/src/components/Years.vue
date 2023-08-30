<script lang="ts" setup>
import { GET } from '@/http/http'
import { computed, ref, type Ref, type WritableComputedRef } from 'vue'

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

(async () => {
  years.value = await GET<number[]>('/public/blog/years')
})()

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