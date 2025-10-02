<script lang="ts" setup>
import { reactive, toRefs } from 'vue'
import { GET } from '@/http/http'
import { API_ENDPOINTS } from '@/config/apiConfig'
import type { Visitor } from '@/type/entity'

const blogStat = reactive<Visitor>({
  dayVisit: 0,
  weekVisit: 0,
  monthVisit: 0,
  yearVisit: 0
})

const { dayVisit, weekVisit, monthVisit, yearVisit } = toRefs(blogStat)

;(async () => {
  const data = await GET<Visitor>(API_ENDPOINTS.BLOG_PUBLIC.GET_BLOG_STAT)
  blogStat.dayVisit = data.dayVisit
  blogStat.weekVisit = data.weekVisit
  blogStat.monthVisit = data.monthVisit
  blogStat.yearVisit = data.yearVisit
})()
</script>

<template>
  <el-breadcrumb separator="/" class="visitor-stat">
    <el-breadcrumb-item>Day {{ dayVisit }}</el-breadcrumb-item>
    <el-breadcrumb-item>Week {{ weekVisit }}</el-breadcrumb-item>
    <el-breadcrumb-item>Month {{ monthVisit }}</el-breadcrumb-item>
    <el-breadcrumb-item>Year {{ yearVisit }}</el-breadcrumb-item>
  </el-breadcrumb>
</template>

<style scoped>
.visitor-stat {
  margin: 0 auto;
  width: fit-content;
  margin-top: 1rem;
  margin-bottom: 1rem;
}
</style>
