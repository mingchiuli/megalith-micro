<script lang="ts" setup>
import { reactive, toRefs } from 'vue'
import { GET } from '@/http/http'

import type { Visitor } from '@/type/entity'

const blogStat = reactive<Visitor>({
  "dayVisit": 0,
  "weekVisit": 0,
  "monthVisit": 0,
  "yearVisit": 0
})

const { dayVisit, weekVisit, monthVisit, yearVisit } = toRefs(blogStat);

(async () => {
  const data = await GET<Visitor>('/public/blog/stat')
  blogStat.dayVisit = data.dayVisit
  blogStat.weekVisit = data.weekVisit
  blogStat.monthVisit = data.monthVisit
  blogStat.yearVisit = data.yearVisit
})()

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
  margin-bottom: 1rem
}
</style>