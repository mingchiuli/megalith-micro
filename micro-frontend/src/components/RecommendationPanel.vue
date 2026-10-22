<script lang="ts" setup>
import { useI18n } from 'vue-i18n'
import { useHttp } from '@/http/http'
import { API_ENDPOINTS } from '@/config/apiConfig'
import type { BlogRecommendations } from '@/type/entity'

const props = withDefaults(
  defineProps<{
    blogId?: number | string
    limit?: number
  }>(),
  { limit: 5 }
)

const { t } = useI18n()
const router = useRouter()
const { GET } = useHttp()
const data = ref<BlogRecommendations>({ hot: [], related: [] })
const loading = ref(true)

const load = async () => {
  loading.value = true
  try {
    const url = new URL(API_ENDPOINTS.BLOG_PUBLIC.GET_RECOMMENDATIONS, 'http://localhost')
    url.searchParams.set('limit', String(props.limit))
    if (props.blogId) url.searchParams.set('blogId', String(props.blogId))
    const response = await GET<BlogRecommendations>(url.pathname + url.search)
    data.value = response ?? { hot: [], related: [] }
  } catch {
    data.value = { hot: [], related: [] }
  } finally {
    loading.value = false
  }
}

const open = (id: number) => {
  router.push({ name: 'blog', params: { id } })
}

onMounted(load)
</script>

<template>
  <el-skeleton :loading="loading" animated :rows="3">
    <template #default>
      <section v-if="data.hot.length" class="recommendation-section">
        <div class="recommendation-title">{{ t('admin.recommendations') }}</div>
        <div class="recommendation-grid">
          <el-card
            v-for="item in data.hot"
            :key="`hot-${item.id}`"
            shadow="hover"
            @click="open(item.id)"
          >
            <div class="recommendation-name">{{ item.title }}</div>
            <div v-if="item.description" class="recommendation-desc">
              {{ item.description }}
            </div>
          </el-card>
        </div>
      </section>
      <section v-if="data.related.length" class="recommendation-section">
        <div class="recommendation-title">{{ t('admin.relatedArticles') }}</div>
        <div class="recommendation-grid">
          <el-card
            v-for="item in data.related"
            :key="`related-${item.id}`"
            shadow="hover"
            @click="open(item.id)"
          >
            <div class="recommendation-name">{{ item.title }}</div>
            <div v-if="item.description" class="recommendation-desc">
              {{ item.description }}
            </div>
          </el-card>
        </div>
      </section>
      <slot />
    </template>
  </el-skeleton>
</template>

<style scoped>
.recommendation-section {
  margin: 22px auto;
  max-width: 900px;
}

.recommendation-title {
  margin-bottom: 10px;
  font-size: 18px;
  font-weight: 600;
}

.recommendation-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 10px;
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
