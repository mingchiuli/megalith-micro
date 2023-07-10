<script lang="ts" setup>
import { reactive, toRefs } from 'vue'
import type { AxiosResponse } from 'axios'
import axios from '../axios'

import type { Visitor, Data } from '../type/entity'

const blogStat : Visitor = reactive({
  "dayVisit": 0, 
  "weekVisit": 0, 
  "monthVisit" : 0, 
  "yearVisit" : 0
})

axios.get('/public/blog/stat')
  .then((resp: AxiosResponse<Data<Visitor>>) => {
    const visitor : Visitor = resp.data.data
    blogStat.dayVisit = visitor.dayVisit
    blogStat.weekVisit = visitor.weekVisit
    blogStat.monthVisit = visitor.monthVisit
    blogStat.yearVisit = visitor.yearVisit
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
  margin-top: 1rem;
  margin-bottom: 1rem;
}
</style>