<script lang="ts" setup>
import { GET } from '@/http/http'
import router from '@/router'
import type { BlogsDesc, PageAdapter } from '@/type/entity'
import type { ElAutocomplete } from 'element-plus'
import { computed, ref } from 'vue'

const emit = defineEmits<{
  (event: 'transSearchData', payload: PageAdapter<BlogsDesc>): void
  (event: 'clear'): void
  (event: 'update:year', payload: string): void
  (event: 'update:keywords', payload: string): void
  (event: 'update:loading', payload: boolean): void
  (event: 'update:searchDialogVisible', payload: boolean): void
}>()

const props = defineProps<{
  year: string
  keywords: string
  loading: boolean
  searchDialogVisible: boolean
}>()

let year = computed({
  get() {
    return props.year
  },
  set(value) {
    emit('update:year', value)
  }
})

let keywords = computed({
  get() {
    return props.keywords
  },
  set(value) {
    emit('update:keywords', value)
  }
})

let loading = computed({
  get() {
    return props.loading
  },
  set(value) {
    emit('update:loading', value);
  }
})

let searchDialogVisible = computed({
  get() {
    return props.searchDialogVisible
  },
  set(value) {
    emit('update:searchDialogVisible', value);
  }
})

const yearDialogVisible = ref(false)
const search = async (queryString: string, currentPage: number, allInfo: boolean, year: string): Promise<PageAdapter<BlogsDesc>> => {
  loading.value = true
  const data = await GET<PageAdapter<BlogsDesc>>(`/search/public/blog?keywords=${queryString}&currentPage=${currentPage}&allInfo=${allInfo}&year=${year}`);
  return Promise.resolve(data)
}

let timeout: NodeJS.Timeout
const searchAbstractAsync = async (queryString: string, cb: Function) => {
  if (queryString.length) {
    //-1是后端一个默认参数
    const page: PageAdapter<BlogsDesc> = await search(queryString, -1, false, year.value)
    page.content.forEach((blogsDesc: BlogsDesc) => {
      blogsDesc.value = blogsDesc.highlight
    })
    //节流
    clearTimeout(timeout)
    timeout = setTimeout(() => {
      cb(page.content)
      if (!page.content.length) {
        ElMessage.error('No Records')
      }
    }, 1000 * Math.random())
  }
}

const handleSelect = (item: BlogsDesc) => router.push({
  name: 'blog',
  params: {
    id: item.id
  }
})

const searchAllInfo = async (queryString: string, currentPage = 1) => {
  searchDialogVisible.value = false
  if (queryString.length) {
    const page: PageAdapter<BlogsDesc> = await search(queryString, currentPage, true, year.value)
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
  if (keywords.value.length) {
    setTimeout(() => {
      refAutocomplete.value!.activated = true
    }, 100)
  }
}

const clearSearch = () => {
  keywords.value = ''
  searchDialogVisible.value = false
  emit('clear')
}

defineExpose(
  { searchAllInfo }
)
</script>

<template>
  <el-dialog v-model="searchDialogVisible" center close-on-press-escape fullscreen align-center
    :before-close="searchBeforeClose">
    <template #default>
      <div class="dialog-content">
        <Hot></Hot>
        <div v-if="year.length">年份：{{ year }}</div>
        <el-autocomplete v-model="keywords" :fetch-suggestions="searchAbstractAsync" placeholder="Please input"
          @select="handleSelect" :trigger-on-focus="false" clearable @keyup.enter="searchAllInfo(keywords)"
          ref="refAutocomplete" @clear="clearSearch">
          <template #default="{ item }">
            <div class="value" v-if="item.value.title" v-for="title in item.value.title" v-html="'标题：' + title"></div>
            <div class="value" v-if="item.value.description" v-for="description in item.value.description"
              v-html="'摘要：' + description"></div>
            <div class="value" v-if="item.value.content" v-for="content in item.value.content" v-html="'内容：' + content">
            </div>
          </template>
        </el-autocomplete>
      </div>
      <Years v-model:year="year" v-model:yearDialogVisible="yearDialogVisible" @close="yearsCloseEvent"></Years>
    </template>
    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" @click="searchAllInfo(keywords)">Confirm</el-button>
        <el-button type="primary" @click="yearDialogVisible = true">Archieve</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.dialog-content {
  margin: 0 auto;
  max-width: max-content;
}

.el-overlay-dialog .dialog-content {
  text-align: center;
  margin-top: 20px
}
</style>