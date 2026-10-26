<script lang="ts" setup>
import type { BlogRecommendation } from '@/type/entity'

withDefaults(
  defineProps<{
    items: BlogRecommendation[]
    layout?: 'single' | 'auto'
  }>(),
  { layout: 'auto' }
)

const emit = defineEmits<{
  select: [id: number]
}>()
</script>

<template>
  <div class="recommendation-grid" :class="`recommendation-grid--${layout}`">
    <el-card v-for="item in items" :key="item.id" shadow="hover" @click="emit('select', item.id)">
      <div class="recommendation-name">{{ item.title }}</div>
      <div v-if="item.description" class="recommendation-desc">
        {{ item.description }}
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.recommendation-grid {
  display: grid;
  gap: 10px;
}

.recommendation-grid--auto {
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
}

.recommendation-grid--single {
  grid-template-columns: minmax(0, 1fr);
}

.recommendation-grid .el-card {
  cursor: pointer;
}

.recommendation-name {
  font-weight: 600;
}

.recommendation-desc {
  margin-top: 6px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
</style>
