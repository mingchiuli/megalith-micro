<script lang="ts" setup>
import axios from '../axios'
import { ref, type Ref } from 'vue'
import type { AxiosResponse } from 'axios'
import type { Data } from "../type/entity"

const emit = defineEmits<(event: "choose-year", payload: string) => void>()

let years: Ref<number[]> = ref([])

axios.get('/public/blog/years')
  .then((resp: AxiosResponse<Data<number[]>>) => {
    years.value = resp.data.data
  })

const chooseYear: Function = (year: number) => {
  emit("choose-year", year.toString())
}

const clearYear: Function = () => emit("choose-year", '')

</script>

<template>
  <el-button type="primary" v-for="year in years" @click="chooseYear(year)">{{ year }}</el-button>
  <el-button type="primary" @click="clearYear">清除</el-button>
</template>

<style scoped>
.el-button {
  margin: 5px;
}
</style>