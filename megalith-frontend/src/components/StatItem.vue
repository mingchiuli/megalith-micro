<script lang="ts" setup>
import { API_CONFIG, API_ENDPOINTS } from '@/config/apiConfig'
import { createTraceParent } from '@/config/otel'
import type { Data, Visitor } from '@/type/entity'

const blogStat = reactive<Visitor>({
  dayVisit: 0,
  weekVisit: 0,
  monthVisit: 0,
  yearVisit: 0
})

const { dayVisit, weekVisit, monthVisit, yearVisit } = toRefs(blogStat)

let disposed = false
let cancelScheduledLoad: (() => void) | undefined

const loadStatistics = async () => {
  const response = await fetch(`${API_CONFIG.BASE_URL}${API_ENDPOINTS.BLOG_PUBLIC.GET_BLOG_STAT}`, {
    headers: { traceparent: createTraceParent() }
  })
  if (!response.ok) return
  const { data } = (await response.json()) as Data<Visitor>
  if (disposed) return
  Object.assign(blogStat, data)
}

onMounted(() => {
  const run = () => void loadStatistics().catch(() => undefined)
  if (typeof window.requestIdleCallback === 'function') {
    const handle = window.requestIdleCallback(run, { timeout: 1500 })
    cancelScheduledLoad = () => window.cancelIdleCallback(handle)
    return
  }

  const handle = globalThis.setTimeout(run, 200)
  cancelScheduledLoad = () => globalThis.clearTimeout(handle)
})

onBeforeUnmount(() => {
  disposed = true
  cancelScheduledLoad?.()
})
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
