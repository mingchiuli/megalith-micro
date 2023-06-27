<script lang="ts" setup>
import { inject, reactive, toRefs } from 'vue';

type stat = {
  daySize: number, 
  weekSize: number, 
  monthSize: number, 
  yearSize: number
}
const blogStat : stat = reactive({
  "daySize": 0, 
  "weekSize" : 0, 
  "monthSize" : 0, 
  "yearSize" : 0
})

const axios : any = inject('$axios')
axios.get('/public/blog/stat').then(async (resp: Promise<stat>) => {
  const data = await resp
  blogStat.daySize = data.daySize
  blogStat.weekSize = data.weekSize
  blogStat.monthSize = data.monthSize
  blogStat.yearSize = data.yearSize
})

const {daySize, weekSize, monthSize,yearSize} = toRefs(blogStat)
</script>

<template>
  <el-breadcrumb separator="/" class="vistor-stat">
    <el-breadcrumb-item>访客数</el-breadcrumb-item>
    <el-breadcrumb-item>本日：{{ daySize }}</el-breadcrumb-item>
    <el-breadcrumb-item>本周：{{ weekSize }}</el-breadcrumb-item>
    <el-breadcrumb-item>本月：{{ monthSize }}</el-breadcrumb-item>
    <el-breadcrumb-item>本年：{{ yearSize }}</el-breadcrumb-item>
  </el-breadcrumb>  
</template>

<style scoped>
.vistor-stat {
  margin: 0 auto;
  width: fit-content;
  margin-top: 30px;
  margin-bottom: 30px;
}
</style>