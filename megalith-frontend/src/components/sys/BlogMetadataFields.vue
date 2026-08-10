<script lang="ts" setup>
import type { ElInput, TagProps } from 'element-plus'
import { SensitiveType, Status, type SensitiveExhibit, type SensitiveTrans } from '@/type/entity'
import { useI18n } from 'vue-i18n'

defineProps<{
  manageMetadata: boolean
  sensitiveTags: Array<{ element: SensitiveExhibit; type: TagProps['type'] }>
}>()

const emit = defineEmits<{
  sensitive: [payload: SensitiveTrans]
  removeSensitive: [type: SensitiveType, startIndex: number]
}>()

const title = defineModel<string>('title', { required: true })
const description = defineModel<string>('description', { required: true })
const status = defineModel<Status>('status', { required: true })
const { t } = useI18n()
const titleRef = useTemplateRef<InstanceType<typeof ElInput>>('titleRef')
const descRef = useTemplateRef<InstanceType<typeof ElInput>>('descRef')

const emitSelection = (input: HTMLInputElement | HTMLTextAreaElement, type: SensitiveType) => {
  if (status.value !== Status.SENSITIVE_FILTER) return
  const start = Math.min(input.selectionStart ?? 0, input.selectionEnd ?? 0)
  const end = Math.max(input.selectionStart ?? 0, input.selectionEnd ?? 0)
  const source = type === SensitiveType.TITLE ? title.value : description.value
  if (source.substring(start, end)) emit('sensitive', { startIndex: start, endIndex: end, type })
}
</script>

<template>
  <el-form-item class="title" prop="title">
    <el-input
      ref="titleRef"
      v-model="title"
      :placeholder="t('common.title')"
      maxlength="20"
      :disabled="!manageMetadata"
      @select="emitSelection(titleRef!.input!, SensitiveType.TITLE)"
    />
  </el-form-item>

  <div class="desc-input-group">
    <el-form-item class="desc" prop="description">
      <el-input
        ref="descRef"
        v-model="description"
        autosize
        type="textarea"
        :placeholder="t('common.description')"
        maxlength="60"
        :disabled="!manageMetadata"
        @select="emitSelection(descRef!.textarea!, SensitiveType.DESCRIPTION)"
      />
    </el-form-item>

    <slot name="description-actions" />
  </div>

  <slot name="after-description" />

  <el-form-item class="status" prop="status">
    <el-radio-group v-model="status" :disabled="!manageMetadata">
      <el-radio :value="Status.NORMAL">{{ t('common.public') }}</el-radio>
      <el-radio :value="Status.BLOCK">{{ t('common.hidden') }}</el-radio>
      <el-radio :value="Status.SENSITIVE_FILTER">{{ t('common.masked') }}</el-radio>
      <el-radio :value="Status.DRAFT">{{ t('common.draft') }}</el-radio>
    </el-radio-group>
  </el-form-item>

  <el-form-item v-if="status === Status.SENSITIVE_FILTER" :label="t('common.masked')">
    <el-popover
      v-for="tag in sensitiveTags"
      :key="`${tag.element.type}-${tag.element.startIndex}`"
      placement="top-start"
      trigger="hover"
      :content="tag.element.content"
    >
      <template #reference>
        <el-tag
          closable
          :type="tag.type"
          @close="emit('removeSensitive', tag.element.type, tag.element.startIndex)"
        >
          {{ tag.element.startIndex }}
        </el-tag>
      </template>
    </el-popover>
  </el-form-item>
</template>

<style scoped>
.title {
  display: flex;
  width: 100%;
  max-width: 200px;
  min-width: 0;
  margin-top: 15px;
}

.desc {
  flex: 1;
  min-width: 0;
  margin: 0;
}

.desc-input-group {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  width: 100%;
  max-width: 800px;
  margin-top: 25px;
}

.desc-input-group .el-input {
  width: 100%;
}

.status {
  display: flex;
  width: 100%;
  max-width: 300px;
  min-width: 0;
  margin-top: 25px;
}

.title :deep(.el-form-item__content),
.status :deep(.el-form-item__content) {
  flex: 1;
  min-width: 0;
}

.status :deep(.el-radio-group) {
  display: flex;
  flex-wrap: wrap;
}

.el-tag {
  margin: 5px;
}
</style>
