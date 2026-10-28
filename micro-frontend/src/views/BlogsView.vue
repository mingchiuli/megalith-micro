<script lang="ts" setup>
import type { BlogDesc, BlogRecommendation, PageAdapter, SearchPage } from '@/type/entity'
import { useHttp } from '@/http/http'
import { loginStateStore, tabStore, blogsStore, menuStore, themeStore } from '@/stores'
import Search from '@/components/SearchItem.vue'
import InfiniteScrollFooter from '@/components/InfiniteScrollFooter.vue'
import { Status } from '@/type/entity'
import { API_ENDPOINTS, buildCommonUrls } from '@/config/apiConfig'
import { Moon, Sunny } from '@element-plus/icons-vue'
import { sanitizeHighlight } from '@/utils/sanitize'
import { useUniversalData } from '@/composables'

type FeedPayload =
  | { kind: 'recommendations'; page: PageAdapter<BlogRecommendation> }
  | { kind: 'search'; page: SearchPage<BlogDesc> }

const IMAGE_LOAD_TIMEOUT_MS = 10_000
const requestLoading = ref(true)
const loadingMore = ref(false)
const imageLoading = ref(false)
const feedError = ref(false)
const { GET } = useHttp()
const router = useRouter()
const route = useRoute()
const searchDialogVisible = ref(false)
const searchRef = useTemplateRef<InstanceType<typeof Search>>('searchRef')
const readTokenDialogVisible = ref(false)
const blogId = ref(0)
const recommendations = ref<BlogRecommendation[]>([])
const searchPage: PageAdapter<BlogDesc> = reactive({
  content: [],
  totalElements: 0,
  pageSize: 5,
  pageNumber: 1
})
const recommendationHasMore = ref(false)
const searchHasMore = ref(false)
let feedGeneration = 0

const { searchPageNum, pageNum, keywords } = storeToRefs(blogsStore())
pageNum.value = 1
searchPageNum.value = 1
keywords.value = typeof route.query.q === 'string' ? route.query.q : ''

const { login } = storeToRefs(loginStateStore())
const theme = themeStore()
const { isDark } = storeToRefs(theme)

// 推荐项与筛选结果共用同一套文章卡片样式
const feedCards = computed<BlogDesc[]>(() =>
  keywords.value
    ? searchPage.content
    : recommendations.value.map((item) => ({ ...item, status: Status.NORMAL }))
)
const hasContent = computed(() => feedCards.value.length > 0)
const hasMore = computed(() => (keywords.value ? searchHasMore.value : recommendationHasMore.value))
const paginationLoading = computed(
  () => loadingMore.value || (requestLoading.value && hasContent.value)
)
const skeletonLoading = computed(
  () => imageLoading.value || (requestLoading.value && !hasContent.value)
)

// 初始化主题
onMounted(() => {
  theme.initTheme()
})

let pendingImageIds = new Set<number>()
let imageLoadTimeout: ReturnType<typeof globalThis.setTimeout> | undefined

const clearImageLoadTimeout = () => {
  if (imageLoadTimeout === undefined) return
  globalThis.clearTimeout(imageLoadTimeout)
  imageLoadTimeout = undefined
}

const finishImageBatch = () => {
  clearImageLoadTimeout()
  pendingImageIds.clear()
  imageLoading.value = false
}

const startImageBatch = (items: BlogDesc[]) => {
  clearImageLoadTimeout()
  pendingImageIds = new Set(items.filter((item) => item.link).map((item) => item.id))
  imageLoading.value = pendingImageIds.size > 0
  if (import.meta.env.SSR || !imageLoading.value) return
  imageLoadTimeout = globalThis.setTimeout(finishImageBatch, IMAGE_LOAD_TIMEOUT_MS)
}

const settleImage = (id: number) => {
  if (!pendingImageIds.delete(id) || pendingImageIds.size) return
  finishImageBatch()
}

onUnmounted(clearImageLoadTimeout)

const appendUnique = <T extends { id: number }>(current: T[], incoming: T[]) => {
  const ids = new Set(current.map((item) => item.id))
  return [...current, ...incoming.filter((item) => !ids.has(item.id))]
}

const applySearchPage = (data: PageAdapter<BlogDesc>) => {
  const reset = data.pageNumber <= 1
  searchPage.content = reset ? data.content : appendUnique(searchPage.content, data.content)
  searchPage.totalElements = data.totalElements
  searchPage.pageSize = data.pageSize
  searchPage.pageNumber = data.pageNumber
  searchPageNum.value = data.pageNumber
  searchHasMore.value = data.last === undefined ? data.content.length >= data.pageSize : !data.last
  feedError.value = false
  if (reset) startImageBatch(data.content)
}

const applyRecommendationPage = (data: PageAdapter<BlogRecommendation>) => {
  const reset = data.pageNumber <= 1
  recommendations.value = reset ? data.content : appendUnique(recommendations.value, data.content)
  pageNum.value = data.pageNumber
  recommendationHasMore.value =
    data.last === undefined ? data.content.length >= data.pageSize : !data.last
  feedError.value = false
}

const applyFeedPayload = (payload: FeedPayload) => {
  if (payload.kind === 'search') applySearchPage(payload.page)
  else applyRecommendationPage(payload.page)
}

const loadActiveFeed = async (): Promise<FeedPayload> => {
  feedError.value = false
  try {
    if (keywords.value) {
      return {
        kind: 'search',
        page: await GET<SearchPage<BlogDesc>>(
          buildCommonUrls.searchQuery({
            keywords: keywords.value,
            currentPage: 1,
            allInfo: true
          })
        )
      }
    }
    return {
      kind: 'recommendations',
      page: await GET<PageAdapter<BlogRecommendation>>(
        API_ENDPOINTS.BLOG_PUBLIC.GET_RECOMMENDATIONS_PAGE(1)
      )
    }
  } catch (cause) {
    feedError.value = true
    throw cause
  }
}

const initialKey = keywords.value
  ? `blog-feed:search:${keywords.value}:1`
  : 'blog-feed:recommendations:1'
const { refresh: refreshFeed } = useUniversalData<FeedPayload>(
  initialKey,
  loadActiveFeed,
  applyFeedPayload,
  { loading: requestLoading }
)

const fillSearchData = (payload: PageAdapter<BlogDesc>) => {
  if (!keywords.value) return
  if (payload.pageNumber <= 1) feedGeneration += 1
  applySearchPage(payload)
}

const loadRecommendationPage = async (page: number) => {
  const generation = feedGeneration
  loadingMore.value = true
  feedError.value = false
  try {
    const data = await GET<PageAdapter<BlogRecommendation>>(
      API_ENDPOINTS.BLOG_PUBLIC.GET_RECOMMENDATIONS_PAGE(page)
    )
    if (generation !== feedGeneration || keywords.value) return
    applyRecommendationPage(data)
  } catch {
    if (generation === feedGeneration && !keywords.value) feedError.value = true
  } finally {
    loadingMore.value = false
  }
}

const loadMore = async () => {
  if (paginationLoading.value || feedError.value || !hasMore.value) return
  if (!keywords.value) {
    await loadRecommendationPage(pageNum.value + 1)
    return
  }

  loadingMore.value = true
  try {
    await nextTick()
    await searchRef.value?.searchAllInfo(keywords.value, searchPageNum.value + 1)
  } finally {
    loadingMore.value = false
  }
}

const retryFeed = () => {
  feedError.value = false
  if (hasContent.value) {
    void loadMore()
    return
  }
  imageLoading.value = false
  void refreshFeed().catch(() => undefined)
}

const highlighted = (label: string, html: string) => sanitizeHighlight(label + html)

const search = () => {
  searchDialogVisible.value = true
}

const to = (id: number, status: Status) => {
  if (status === Status.NORMAL || status === Status.SENSITIVE_FILTER) {
    void router.push({ name: 'blog', params: { id } })
  } else {
    readTokenDialogVisible.value = true
    blogId.value = id
  }
}

const clearSearch = () => {
  searchRef.value?.cancelSearch()
  feedGeneration += 1
  keywords.value = ''
  searchPageNum.value = 1
  pageNum.value = 1
  searchPage.content = []
  searchPage.totalElements = 0
  recommendations.value = []
  recommendationHasMore.value = false
  loadingMore.value = false
  feedError.value = false
  finishImageBatch()
  void refreshFeed().catch(() => undefined)
}

const goToBack = () => {
  void router.push({
    name: tabStore().editableTabsValue ? tabStore().editableTabsValue : menuStore().menuTree!.name
  })
}
</script>

<template>
  <div class="front">
    <ReadTokenItem v-model:read-token-dialog-visible="readTokenDialogVisible" :blog-id="blogId" />
    <div class="search-father">
      <el-button class="search-button" @click="search" type="success">{{
        $t('common.search')
      }}</el-button>
      <el-button
        class="theme-button"
        @click="theme.toggleTheme"
        :icon="isDark ? Sunny : Moon"
        circle
      />
      <SearchItem
        ref="searchRef"
        @trans-search-data="fillSearchData"
        @refresh="clearSearch"
        @search-error="feedError = true"
        v-model:loading="requestLoading"
        v-model:search-dialog-visible="searchDialogVisible"
      />
    </div>
    <el-link
      type="success"
      size="large"
      class="door"
      v-if="login && menuStore().menuTree"
      @click="goToBack"
      >{{ $t('blog.enterAdmin') }}</el-link
    >
    <el-link class="door" type="warning" v-if="keywords" link @click="clearSearch">{{
      $t('common.back')
    }}</el-link>
    <br />

    <section class="description">
      <el-skeleton class="blogs-skeleton" animated :loading="skeletonLoading">
        <template #template>
          <el-skeleton v-for="i in 3" :key="i" :rows="10" animated />
        </template>
        <el-timeline>
          <el-timeline-item
            v-for="blog in feedCards"
            :key="blog.id"
            :timestamp="blog.created"
            placement="top"
            :color="'#0bbd87'"
            v-show="!skeletonLoading"
          >
            <el-card shadow="hover" @click="to(blog.id, blog.status)">
              <el-image
                v-if="blog.link"
                :key="blog.link"
                :src="blog.link"
                @load="settleImage(blog.id)"
                @error="settleImage(blog.id)"
              ></el-image>
              <p v-if="blog.score">
                {{ $t('blog.searchScore', { score: blog.score }) }}
              </p>
              <el-link class="title">{{ blog.title }}</el-link>
              <p v-if="!blog.highlight">{{ blog.description }}</p>
              <template v-if="blog.highlight?.title">
                <p
                  v-for="(title, key) in blog.highlight.title"
                  v-bind:key="key"
                  v-html="highlighted(`${$t('common.title')}: `, title)"
                ></p>
              </template>
              <template v-if="blog.highlight?.description">
                <p
                  v-for="(description, key) in blog.highlight.description"
                  v-bind:key="key"
                  v-html="highlighted(`${$t('common.description')}: `, description)"
                ></p>
              </template>
              <template v-if="blog.highlight?.content">
                <p
                  v-for="(content, key) in blog.highlight.content"
                  v-bind:key="key"
                  v-html="highlighted(`${$t('common.content')}: `, content)"
                ></p>
              </template>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </el-skeleton>
      <p v-if="!skeletonLoading && !hasContent && !feedError" class="feed-empty">
        {{ keywords ? $t('blog.noSearchResults') : $t('blog.noRecommendations') }}
      </p>
    </section>

    <InfiniteScrollFooter
      v-if="(hasContent || feedError) && !skeletonLoading"
      :loading="paginationLoading"
      :has-more="hasMore"
      :error="feedError"
      :loading-text="$t('blog.loadingMore')"
      :error-text="$t('blog.loadMoreFailed')"
      :retry-text="$t('blog.retry')"
      :end-text="$t('blog.noMore')"
      @load="loadMore"
      @retry="retryFeed"
    />
  </div>
</template>

<style scoped>
@import '@/assets/front.css';

.door {
  width: fit-content;
  margin-left: 5px;
  margin-right: 5px;
  margin-bottom: 5px;
}

.search-father {
  margin-top: 20px;
  position: relative;
}

.description {
  margin: 0 auto;
}

.feed-empty {
  padding: 28px 0;
  color: var(--el-text-color-secondary);
  text-align: center;
}

.el-timeline:deep(.el-timeline-item__wrapper) {
  padding-left: 5%;
}

.el-timeline {
  padding: 0;
}

.title {
  font-size: medium;
  margin-top: 15px;
  display: block;
  max-width: fit-content;
}

.search-button {
  position: absolute;
  right: 0;
  top: 5px;
  z-index: 1;
}

.theme-button {
  position: absolute;
  right: 90px;
  top: 5px;
  z-index: 1;
}
</style>
