<script lang="ts" setup>
import { useHttp } from '@/http/http'
import type { BlogDesc, PageAdapter, SearchPage } from '@/type/entity'
import type {
  AutocompleteFetchSuggestions,
  AutocompleteFetchSuggestionsCallback
} from 'element-plus'
import type HotItem from '@/components/HotItem.vue'
import { blogsStore } from '@/stores'
import { buildCommonUrls } from '@/config/apiConfig'
import { sanitizeHighlight } from '@/utils/sanitize'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const { GET } = useHttp()
const router = useRouter()
const emit = defineEmits<{
  transSearchData: [payload: PageAdapter<BlogDesc>]
  refresh: [payload: void]
}>()

const { keywords } = storeToRefs(blogsStore())
const loading = defineModel<boolean>('loading')
const searchDialogVisible = defineModel<boolean>('searchDialogVisible')
const suggestionList = ref<BlogDesc[]>([])
const hotItemRef = useTemplateRef<InstanceType<typeof HotItem>>('hotItem')

const search = async (
  queryString: string,
  currentPage: number,
  allInfo: boolean,
  signal?: AbortSignal
): Promise<SearchPage<BlogDesc>> => {
  const url = buildCommonUrls.searchQuery({
    keywords: queryString,
    currentPage,
    allInfo
  })
  return GET<SearchPage<BlogDesc>>(url, { signal })
}

let suggestionRequest = 0
let suggestionController: AbortController | null = null

const searchAbstractAsync: AutocompleteFetchSuggestions = (
  queryString: string,
  cb: AutocompleteFetchSuggestionsCallback
) => {
  const normalizedQuery = queryString.trim()
  suggestionController?.abort()
  const requestId = ++suggestionRequest

  if (!normalizedQuery) {
    suggestionList.value = []
    cb([])
    return
  }

  suggestionController = new AbortController()
  search(normalizedQuery, 1, false, suggestionController.signal)
    .then((page) => {
      if (requestId !== suggestionRequest) return
      suggestionList.value = page.content.map((blog) => ({
        ...blog,
        value: normalizedQuery
      }))
      cb(suggestionList.value)
    })
    .catch(() => {
      if (requestId !== suggestionRequest) return
      suggestionList.value = []
      cb([])
    })
}

const handleSelect = (item: Record<string, string | number>) => {
  router.push({
    name: 'blog',
    params: {
      id: item.id
    }
  })
}

const searchAllInfo = async (queryString: string, currentPage = 1) => {
  searchDialogVisible.value = false
  // Reset searchPageNum when starting a new search from the dialog
  if (currentPage === 1) {
    blogsStore().searchPageNum = 1
  }
  try {
    if (queryString.length) {
      loading.value = true
      const page: PageAdapter<BlogDesc> = await search(queryString, currentPage, true)
      if (page.content.length) {
        emit('transSearchData', page)
        return
      }
    }
    emit('refresh')
  } catch {
    loading.value = false
  }
}

const searchBeforeClose = (close: () => void) => {
  keywords.value = ''
  suggestionController?.abort()
  suggestionList.value = []
  emit('refresh')
  close()
}

const openDialog = () => {
  hotItemRef.value!.load()
}

const clearSearch = () => {
  keywords.value = ''
  suggestionController?.abort()
  suggestionList.value = []
}

const highlighted = (label: string, html: string) => sanitizeHighlight(label + html)

onBeforeUnmount(() => {
  suggestionController?.abort()
})

defineExpose({
  searchAllInfo,
  searchAbstractAsync,
  handleSelect,
  searchBeforeClose,
  openDialog,
  clearSearch,
  highlighted
})
</script>

<template>
  <el-dialog
    v-model="searchDialogVisible"
    center
    close-on-press-escape
    fullscreen
    align-center
    :before-close="searchBeforeClose"
    @open="openDialog"
  >
    <template #default>
      <HotItem ref="hotItem" class="dialog-hot" />
      <div class="dialog-autocomplete">
        <el-autocomplete
          id="elc"
          v-model="keywords"
          :fetch-suggestions="searchAbstractAsync"
          :debounce="300"
          :placeholder="t('common.input')"
          placement="bottom"
          @select="handleSelect"
          :trigger-on-focus="false"
          popper-class="select-list"
          clearable
          @keyup.enter="searchAllInfo(keywords!)"
          @clear="clearSearch"
          :fit-input-width="true"
        >
          <template #default="{ item }">
            <template v-if="item.highlight.title">
              <div
                class="value"
                v-for="(title, key) in item.highlight.title"
                v-bind:key="key"
                v-html="highlighted(`${t('common.title')}：`, title)"
              />
            </template>
            <template v-if="item.highlight.description">
              <div
                class="value"
                v-for="(description, key) in item.highlight.description"
                v-bind:key="key"
                v-html="highlighted(`${t('common.description')}：`, description)"
              />
            </template>
            <template v-if="item.highlight.content">
              <div
                class="value"
                v-for="(content, key) in item.highlight.content"
                v-bind:key="key"
                v-html="highlighted(`${t('common.content')}：`, content)"
              />
            </template>
          </template>
          <template #loading>
            <svg class="circular" viewBox="0 0 50 50">
              <circle class="path" cx="25" cy="25" r="20" fill="none" />
            </svg>
          </template>
        </el-autocomplete>
      </div>
    </template>
    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" @click="searchAllInfo(keywords!)">{{
          t('common.confirm')
        }}</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.value {
  overflow-x: auto;
}

.dialog-autocomplete {
  margin: 10px auto 0 auto;
  max-width: max-content;
}

.dialog-hot {
  margin: 0 auto;
}

.circular {
  display: inline;
  height: 30px;
  width: 30px;
  animation: loading-rotate 2s linear infinite;
}

.path {
  animation: loading-dash 1.5s ease-in-out infinite;
  stroke-dasharray: 90, 150;
  stroke-dashoffset: 0;
  stroke-width: 2;
  stroke: var(--el-color-primary);
  stroke-linecap: round;
}
</style>
