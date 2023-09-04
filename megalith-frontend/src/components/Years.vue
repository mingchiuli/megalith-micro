<script lang="ts" setup>
import { GET } from '@/http/http'
import { computed, ref } from 'vue'

const props = defineProps<{
  year: string
}>()

const emit = defineEmits<{
  (event: 'close'): void
  (event: 'update:year', payload: string): void
}>()

let years = ref<number[]>()

let year = computed({
  get() {
    return props.year
  },
  set(value) {
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