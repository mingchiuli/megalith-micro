<script lang="ts" setup>
import type { AiModel } from '@/type/entity'
import { ButtonAuth } from '@/type/entity'
import { checkButtonAuth } from '@/utils/permissions'
import { useI18n } from 'vue-i18n'

defineProps<{
  models: AiModel[]
  loading: boolean
  contentReady: boolean
  manageMetadata: boolean
}>()

const emit = defineEmits<{ generate: [] }>()
const model = defineModel<string>('model', { required: true })
const { t } = useI18n()
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
      ✨AI
    </el-button>
  </div>
</template>

<style scoped>
.ai-actions {
  display: flex;
  flex: none;
  gap: 12px;
  align-items: center;
}

.el-select {
  width: 140px;
}
</style>
