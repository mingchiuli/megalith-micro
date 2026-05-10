<script lang="ts" setup>
import { Status } from '@/type/entity'

const props = defineProps<{
  status: Status
  type?: 'blog' | 'user'
}>()

const getStatusInfo = (status: Status, displayType: string = 'blog') => {
  const blogLabels: Record<number, { label: string; type: 'success' | 'danger' | 'warning' | 'info' }> = {
    [Status.NORMAL]: { label: '公开', type: 'success' },
    [Status.BLOCK]: { label: '隐藏', type: 'danger' },
    [Status.SENSITIVE_FILTER]: { label: '打码', type: 'warning' },
    [Status.DRAFT]: { label: '草稿', type: 'info' }
  }
  const userLabels: Record<number, { label: string; type: 'success' | 'danger' }> = {
    [Status.NORMAL]: { label: '启用', type: 'success' },
    [Status.BLOCK]: { label: '停用', type: 'danger' }
  }

  const labels = displayType === 'user' ? userLabels : blogLabels
  return labels[status] || { label: '未知', type: 'info' }
}

const statusInfo = () => getStatusInfo(props.status, props.type)
</script>

<template>
  <el-tag size="small" :type="statusInfo().type">
    {{ statusInfo().label }}
  </el-tag>
</template>