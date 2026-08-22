<script lang="ts" setup>
import { API_ENDPOINTS } from '@/config/apiConfig'
import type { Visitor } from '@/type/entity'
import { useHttp } from '@/http/http'
import { useUniversalData } from '@/composables'

const blogStat = reactive<Visitor>({
  dayVisit: 0,
  weekVisit: 0,
  monthVisit: 0,
  yearVisit: 0
})

const { dayVisit, weekVisit, monthVisit, yearVisit } = toRefs(blogStat)
const { GET } = useHttp()

useUniversalData(
  'blog-statistics',
  () => GET<Visitor>(API_ENDPOINTS.BLOG_PUBLIC.GET_BLOG_STAT),
  (data) => Object.assign(blogStat, data)
)
</script>

<template>
  <div class="visitor-stat" role="status">
    <span>{{ $t('stats.day', { count: dayVisit }) }}</span>
    <span>{{ $t('stats.week', { count: weekVisit }) }}</span>
    <span>{{ $t('stats.month', { count: monthVisit }) }}</span>
    <span>{{ $t('stats.year', { count: yearVisit }) }}</span>
  </div>
</template>

<style scoped>
.visitor-stat {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
  width: fit-content;
  min-height: 22px;
  margin: 1rem auto;
  color: var(--el-text-color-regular);
  font-size: 14px;
}

.visitor-stat span + span::before {
  margin-right: 8px;
  color: var(--el-text-color-placeholder);
  content: '/';
}
</style>
