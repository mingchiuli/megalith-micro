<script lang="ts" setup>
import { Status } from '@/type/entity'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const props = defineProps<{
  status: Status
  type?: 'blog' | 'user'
}>()

const getStatusInfo = (status: Status, displayType: string = 'blog') => {
  const blogLabels: Record<
    number,
    { label: string; type: 'success' | 'danger' | 'warning' | 'info' }
  > = {
    [Status.NORMAL]: { label: t('common.public'), type: 'success' },
    [Status.BLOCK]: { label: t('common.hidden'), type: 'danger' },
    [Status.SENSITIVE_FILTER]: { label: t('common.masked'), type: 'warning' },
    [Status.DRAFT]: { label: t('common.draft'), type: 'info' }
  }
  const userLabels: Record<number, { label: string; type: 'success' | 'danger' }> = {
    [Status.NORMAL]: { label: t('common.enabled'), type: 'success' },
    [Status.BLOCK]: { label: t('common.inactive'), type: 'danger' }
  }

  const labels = displayType === 'user' ? userLabels : blogLabels
  return labels[status] || { label: t('common.unknown'), type: 'info' }
}

const statusInfo = computed(() => getStatusInfo(props.status, props.type))
</script>

<template>
  <el-tag size="small" :type="statusInfo.type">
    {{ statusInfo.label }}
  </el-tag>
</template>
