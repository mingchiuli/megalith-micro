<script lang="ts" setup>
import { Colors } from '@/type/entity'
import { render } from '@/utils/markdown'
import { useI18n } from 'vue-i18n'

const props = defineProps<{
  visible: boolean
  step: number
  failedStep: number | null
  error: string
  thinking: string
  imageSkipReason: string
  thinkingSupported: boolean
  imageGenerating: boolean
  imageProgress: number
}>()

const { t } = useI18n()
const thinkingCollapse = ref<string[]>(['thinking'])
const thinkingRef = useTemplateRef<HTMLDivElement>('thinkingRef')

watch(
  () => props.thinking,
  () =>
    nextTick(() => {
      if (thinkingRef.value) thinkingRef.value.scrollTop = thinkingRef.value.scrollHeight
    })
)

const thinkingContent = computed(() => {
  if (props.thinking) return props.thinking
  if (props.error) return t('ai.noThinking')
  return props.thinkingSupported ? t('ai.waitingThinking') : t('ai.unsupportedThinking')
})
const thinkingHtml = computed(() => render(thinkingContent.value))
</script>

<template>
  <div v-if="visible" class="ai-panel">
    <el-steps :active="step - 1" align-center finish-status="success">
      <el-step :title="t('ai.titleSummary')" :status="failedStep === 1 ? 'error' : undefined" />
      <el-step
        :title="t('ai.imagePrompt')"
        :description="imageSkipReason"
        :status="failedStep === 2 ? 'error' : imageSkipReason ? 'wait' : undefined"
      />
      <el-step
        :title="t('ai.coverImage')"
        :status="failedStep === 3 ? 'error' : imageSkipReason ? 'wait' : undefined"
      />
    </el-steps>

    <el-alert
      v-if="error"
      :title="error"
      type="error"
      show-icon
      :closable="false"
      class="ai-error"
    />

    <el-collapse v-model="thinkingCollapse" class="thinking-collapse">
      <el-collapse-item :title="`💭 ${t('ai.thinking')}`" name="thinking">
        <div ref="thinkingRef" class="thinking-content" v-html="thinkingHtml"></div>
      </el-collapse-item>
    </el-collapse>

    <el-form-item v-if="imageGenerating" :label="t('ai.imageProgress')" class="progress">
      <el-progress type="line" :percentage="imageProgress" :color="Colors" />
    </el-form-item>
  </div>
</template>

<style scoped>
.ai-panel {
  width: min(100%, 36rem);
  box-sizing: border-box;
  margin-top: 25px;
  padding: 18px 16px 14px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
}

.ai-panel :deep(.el-step__title) {
  font-size: 13px;
  font-weight: 500;
  line-height: 20px;
}

.ai-panel :deep(.el-step__main) {
  margin-top: 5px;
}

.ai-panel :deep(.el-step__description) {
  padding-right: 0;
  font-size: 12px;
}

.ai-panel .progress {
  width: 100%;
  margin: 14px 0 0;
}

.thinking-collapse {
  margin-top: 14px;
}

.thinking-collapse :deep(.el-collapse-item__header) {
  height: 44px;
  padding: 0 10px;
  font-size: 13px;
  font-weight: 500;
}

.thinking-collapse :deep(.el-collapse-item__content) {
  padding-bottom: 0;
}

.ai-error {
  margin-top: 12px;
}

.thinking-content {
  max-height: 200px;
  overflow-y: auto;
  padding: 10px;
  font-size: 13px;
  line-height: 1.7;
  word-break: break-word;
  color: var(--el-text-color-secondary);
}

.thinking-content :deep(p) {
  margin: 0 0 8px;
}

.thinking-content :deep(p:last-child),
.thinking-content :deep(ul:last-child),
.thinking-content :deep(ol:last-child),
.thinking-content :deep(pre:last-child),
.thinking-content :deep(blockquote:last-child) {
  margin-bottom: 0;
}

.thinking-content :deep(ul),
.thinking-content :deep(ol) {
  margin: 4px 0 8px;
  padding-left: 20px;
}

.thinking-content :deep(li) {
  margin: 2px 0;
}

.thinking-content :deep(li > p) {
  margin: 0;
}

.thinking-content :deep(pre) {
  overflow-x: auto;
  margin: 8px 0;
  padding: 10px 12px;
  border-radius: 4px;
  background: var(--el-fill-color-lighter);
}

.thinking-content :deep(code) {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 12px;
}

.thinking-content :deep(:not(pre) > code) {
  padding: 1px 4px;
  border-radius: 3px;
  background: var(--el-fill-color-lighter);
}

.thinking-content :deep(blockquote) {
  margin: 8px 0;
  padding-left: 10px;
  border-left: 2px solid var(--el-border-color);
  color: var(--el-text-color-secondary);
}

.thinking-content :deep(h1),
.thinking-content :deep(h2),
.thinking-content :deep(h3) {
  margin: 10px 0 6px;
  font-size: 14px;
  line-height: 1.5;
}
</style>
