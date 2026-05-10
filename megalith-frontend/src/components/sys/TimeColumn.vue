<script lang="ts" setup>
import { Timer } from '@element-plus/icons-vue'

defineProps<{
  time: string
}>()
</script>

<template>
  <div class="time-column">
    <el-icon><timer /></el-icon>
    <span class="time-text">{{ time }}</span>
  </div>
</template>

<style scoped>
.time-column {
  display: flex;
  align-items: center;
  justify-content: center;
}

.time-text {
  margin-left: 8px;
}
</style>