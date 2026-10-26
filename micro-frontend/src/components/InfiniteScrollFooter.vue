<script lang="ts" setup>
const props = defineProps<{
  loading: boolean
  hasMore: boolean
  error: boolean
  loadingText: string
  errorText: string
  retryText: string
  endText: string
}>()

const emit = defineEmits<{
  load: []
  retry: []
}>()

const sentinel = useTemplateRef<HTMLElement>('sentinel')
let observer: IntersectionObserver | undefined
let intersecting = false

const requestMore = () => {
  if (!intersecting || props.loading || props.error || !props.hasMore) return
  emit('load')
}

watch(
  () => [props.loading, props.error, props.hasMore] as const,
  () => {
    void nextTick(requestMore)
  }
)

watch(
  sentinel,
  (element, previous) => {
    if (previous) observer?.unobserve(previous)
    if (element) observer?.observe(element)
  },
  { flush: 'post' }
)

onMounted(() => {
  if (import.meta.env.SSR || typeof IntersectionObserver === 'undefined') return
  observer = new IntersectionObserver(
    (entries) => {
      intersecting = entries.some((entry) => entry.isIntersecting)
      requestMore()
    },
    { rootMargin: '240px 0px' }
  )
  if (sentinel.value) observer.observe(sentinel.value)
})

onBeforeUnmount(() => observer?.disconnect())
</script>

<template>
  <div class="infinite-footer">
    <div v-if="error" class="infinite-footer__error">
      <span>{{ errorText }}</span>
      <el-link type="primary" @click="emit('retry')">{{ retryText }}</el-link>
    </div>
    <div v-else-if="loading" class="infinite-footer__loading" role="status">
      <span class="infinite-footer__spinner" aria-hidden="true" />
      {{ loadingText }}
    </div>
    <div v-else-if="!hasMore" class="infinite-footer__end">{{ endText }}</div>
    <div v-else ref="sentinel" class="infinite-footer__sentinel" aria-hidden="true" />
  </div>
</template>

<style scoped>
.infinite-footer {
  min-height: 48px;
  display: grid;
  place-items: center;
  padding: 14px 0;
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.infinite-footer__sentinel {
  width: 100%;
  height: 1px;
}

.infinite-footer__loading,
.infinite-footer__error {
  display: flex;
  align-items: center;
  gap: 10px;
}

.infinite-footer__spinner {
  width: 16px;
  height: 16px;
  border: 2px solid var(--el-border-color-lighter);
  border-top-color: var(--el-color-primary);
  border-radius: 50%;
  animation: infinite-footer-spin 0.8s linear infinite;
}

@keyframes infinite-footer-spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
