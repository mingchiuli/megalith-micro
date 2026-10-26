<script lang="ts" setup>
import { useI18n } from 'vue-i18n'
import { useHttp } from '@/http/http'
import { API_ENDPOINTS } from '@/config/apiConfig'
import type { BlogRecommendation } from '@/type/entity'
import RecommendationCards from '@/components/RecommendationCards.vue'

const props = withDefaults(
  defineProps<{
    blogId: number | string
    limit?: number
  }>(),
  { limit: 4 }
)

const { t } = useI18n()
const router = useRouter()
const { GET } = useHttp()
const related = ref<BlogRecommendation[]>([])
const loading = ref(true)

const load = async () => {
  loading.value = true
  try {
    const url = new URL(
      API_ENDPOINTS.BLOG_PUBLIC.GET_RELATED_ARTICLES(props.blogId),
      'http://localhost'
    )
    url.searchParams.set('limit', String(props.limit))
    const response = await GET<BlogRecommendation[]>(url.pathname + url.search)
    related.value = response ?? []
  } catch {
    related.value = []
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
      <section v-if="related.length" class="recommendation-section">
        <div class="recommendation-title">{{ t('admin.relatedArticles') }}</div>
        <RecommendationCards :items="related" @select="open" />
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
</style>
