<script lang="ts" setup>
import { GET } from '@/http/http'
import router from '@/router'
import type { BlogDesc, PageAdapter } from '@/type/entity'
import type { ElAutocomplete } from 'element-plus'
import { onBeforeUnmount, ref, watch } from 'vue'
import { debounce } from '@/utils/tools'

const emit = defineEmits<{
  transSearchData: [payload: PageAdapter<BlogDesc>]
  clear: [payload: void]
}>()

const year = defineModel<string>('year')
const keywords = defineModel<string>('keywords')
const loading = defineModel<boolean>('loading')
const searchDialogVisible = defineModel<boolean>('searchDialogVisible')
const suggestionList = ref<BlogDesc[]>([])
let currentPage = 1

const yearDialogVisible = ref(false)
const search = async (queryString: string, currentPage: number, allInfo: boolean, year: string): Promise<PageAdapter<BlogDesc>> => {
  loading.value = true
  const data = await GET<PageAdapter<BlogDesc>>(`/search/public/blog?keywords=${queryString}&currentPage=${currentPage}&allInfo=${allInfo}&year=${year}`);
  return Promise.resolve(data)
}

let timeout: NodeJS.Timeout
let suggestionEle: Element | null
const controller = new AbortController()
const { signal } = controller
const searchAbstractAsync = async (queryString: string, cb: Function) => {
  if (queryString.length) {
    //-1是后端一个默认参数
    const page: PageAdapter<BlogDesc> = await search(queryString, currentPage, false, year.value!)
    page.content.forEach((blogsDesc: BlogDesc) => {
      suggestionList.value.push(blogsDesc)
    })
    //节流
    clearTimeout(timeout)
    timeout = setTimeout(() => {
      cb(suggestionList.value)
      if (!page.content.length) {
        ElMessage.error('No Records')
        return
      }
      if (!suggestionEle) {
        suggestionEle = document.querySelector('.select-list .el-autocomplete-suggestion__wrap')
        suggestionEle?.addEventListener('scroll', debounce(() => load(suggestionEle!, cb)), { signal })
      }
    }, 1000 * Math.random())
  }
}

watch(() => keywords.value, () => {
  suggestionList.value.splice(0, suggestionList.value.length)
})

const load = async (e: Element, cb: Function) => {
  if (keywords.value && e.scrollTop + e.clientHeight >= e.scrollHeight - 1) {
    const page: PageAdapter<BlogDesc> = await search(keywords.value!, currentPage + 1, false, year.value!)
    if (page.content.length === 0) return
    if (page.content.length > 0) {
      currentPage++
    }
    page.content.forEach((blogsDesc: BlogDesc) => {
      suggestionList.value.push(blogsDesc)
    })
    cb(suggestionList.value)
  }
}

const handleSelect = (item: BlogDesc) => router.push({
  name: 'blog',
  params: {
    id: item.id
  }
})

const searchAllInfo = async (queryString: string, currentPage = 1) => {
  searchDialogVisible.value = false
  if (queryString.length) {
    const page: PageAdapter<BlogDesc> = await search(queryString, currentPage, true, year.value!)
    keywords.value = queryString
    emit('transSearchData', page)
  } else {
    emit('clear')
  }
}

const searchBeforeClose = (close: Function) => {
  keywords.value = ''
  year.value = ''
  refAutocomplete.value!.suggestions = []
  emit('clear')
  close()
}

const refAutocomplete = ref<InstanceType<typeof ElAutocomplete>>()

const yearsCloseEvent = async () => {
  if (keywords.value!.length) {
    setTimeout(() => {
      refAutocomplete.value!.activated = true
    }, 100)
  }
}

const clearSearch = () => {
  currentPage = 1
  keywords.value = ''
}

onBeforeUnmount(() => {
  controller.abort()
})

defineExpose(
  { searchAllInfo }
)
</script>

<template>
  <el-dialog v-model="searchDialogVisible" center close-on-press-escape fullscreen align-center
    :before-close="searchBeforeClose">
    <template #default>
      <HotItem class="dialog-hot" />
      <div class="dialog-year" v-if="year!.length">年份：{{ year }}</div>
      <div class="dialog-autocomplete">
        <el-autocomplete id="elc" v-model="keywords" :fetch-suggestions="searchAbstractAsync" placeholder="Please input"
          @select="handleSelect" :trigger-on-focus="false" popper-class="select-list" clearable
          @keyup.enter="searchAllInfo(keywords!)" ref="refAutocomplete" @clear="clearSearch">
          <template #default="{ item }">
            <template v-if="item.highlight.title">
              <div class="value" v-for="(title, key) in item.highlight.title" v-bind:key="key" v-html="'标题：' + title" />
            </template>
            <template v-if="item.highlight.description">
              <div class="value" v-for="(description, key) in item.highlight.description" v-bind:key="key"
                v-html="'摘要：' + description" />
            </template>
            <template v-if="item.highlight.content">
              <div id="scroll" class="value" v-for="(content, key) in item.highlight.content" v-bind:key="key"
                v-html="'内容：' + content" />
            </template>
          </template>
          <template #loading>
            <svg class="circular" viewBox="0 0 50 50">
              <circle class="path" cx="25" cy="25" r="20" fill="none" />
            </svg>
          </template>
        </el-autocomplete>
      </div>

      <years-item v-model:year="year" v-model:yearDialogVisible="yearDialogVisible"
        @close="yearsCloseEvent"></years-item>
    </template>
    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" @click="searchAllInfo(keywords!)">Confirm</el-button>
        <el-button type="primary" @click="yearDialogVisible = true">Archieve</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.dialog-year {
  text-align: center;
  margin-top: 5px
}

.dialog-autocomplete {
  margin: 30px auto 0 auto;
  max-width: max-content
}

.dialog-hot {
  margin: 0 auto
}

.circular {
  display: inline;
  height: 30px;
  width: 30px;
  animation: loading-rotate 2s linear infinite
}

.path {
  animation: loading-dash 1.5s ease-in-out infinite;
  stroke-dasharray: 90, 150;
  stroke-dashoffset: 0;
  stroke-width: 2;
  stroke: var(--el-color-primary);
  stroke-linecap: round
}
</style>