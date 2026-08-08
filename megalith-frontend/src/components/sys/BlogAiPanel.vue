<script lang="ts" setup>
import type { AiModel } from '@/type/entity'
import { ButtonAuth, Colors } from '@/type/entity'
import { checkButtonAuth } from '@/utils/permissions'
import { render } from '@/utils/markdown'
import { useI18n } from 'vue-i18n'

const props = defineProps<{
  models: AiModel[]
  loading: boolean
  contentReady: boolean
  manageMetadata: boolean
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

const emit = defineEmits<{ generate: [] }>()
const model = defineModel<string>('model', { required: true })
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
  <div class="ai-actions">
    <el-select v-model="model" :placeholder="t('ai.model')" :disabled="loading">
      <el-option v-for="item in models" :key="item.name" :label="item.name" :value="item.model" />
    </el-select>
    <el-button
      v-if="checkButtonAuth(ButtonAuth.SYS_EDIT_AI)"
      color="#626aef"
      size="small"
      :loading="loading"
      :disabled="!manageMetadata || loading || !contentReady || !model"
      @click="emit('generate')"
    >
      {{ t('ai.contentGeneration') }}
    </el-button>
  </div>

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
      <el-collapse-item :title="t('ai.thinking')" name="thinking">
        <div ref="thinkingRef" class="thinking-content" v-html="thinkingHtml"></div>
      </el-collapse-item>
    </el-collapse>

    <el-form-item v-if="imageGenerating" :label="t('ai.imageProgress')" class="progress">
      <el-progress type="line" :percentage="imageProgress" :color="Colors" />
    </el-form-item>
  </div>
</template>

<style scoped>
.ai-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  margin-top: 12px;
}

.ai-actions .el-select {
  width: 140px;
}

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

.progress {
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
  color: var(--el-text-color-secondary);
  font-size: 13px;
  line-height: 1.7;
  overflow-wrap: anywhere;
}

.thinking-content :deep(pre) {
  overflow-x: auto;
  padding: 10px 12px;
  border-radius: 4px;
  background: var(--el-fill-color-lighter);
}
</style>
