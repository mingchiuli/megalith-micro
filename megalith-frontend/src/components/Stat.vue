<script lang="ts" setup>
import { inject, reactive, toRefs } from 'vue';

type stat = {
  dayVisit: number, 
  weekVisit: number, 
  monthVisit: number, 
  yearVisit: number
}
const blogStat : stat = reactive({
  "dayVisit": 0, 
  "weekVisit": 0, 
  "monthVisit" : 0, 
  "yearVisit" : 0
})

const axios : any = inject('$axios')
axios.get('/public/blog/stat').then(async (resp: Promise<stat>) => {
  const data = await resp
  blogStat.dayVisit = data.dayVisit
  blogStat.weekVisit = data.weekVisit
  blogStat.monthVisit = data.monthVisit
  blogStat.yearVisit = data.yearVisit
})

const {dayVisit, weekVisit, monthVisit, yearVisit} = toRefs(blogStat)
</script>

<template>
  <el-breadcrumb separator="/" class="vistor-stat">
    <el-breadcrumb-item>访客数</el-breadcrumb-item>
    <el-breadcrumb-item>本日：{{ dayVisit }}</el-breadcrumb-item>
    <el-breadcrumb-item>本周：{{ weekVisit }}</el-breadcrumb-item>
    <el-breadcrumb-item>本月：{{ monthVisit }}</el-breadcrumb-item>
    <el-breadcrumb-item>本年：{{ yearVisit }}</el-breadcrumb-item>
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