<script lang="ts" setup>
import type { BlogsDesc, Data, PageAdapter } from '@/type/entity'
import axios from '@/axios'
import { ref, type Ref } from 'vue'
import type { AxiosResponse } from 'axios'
import { searchStore } from '@/stores/store'
import { storeToRefs } from 'pinia'

const emit = defineEmits<{
  (event: 'search', payload: PageAdapter<BlogsDesc>): void
  (event: 'clear'): void
}>()

const store = searchStore()
const { year, keywords } = storeToRefs(store)

const outerVisible: Ref<boolean> = ref(false)
const innerVisible: Ref<boolean> = ref(false)

const query = async (queryString: string, currentPage: number, allInfo: boolean, year: string): Promise<PageAdapter<BlogsDesc>> => {
  const resp: AxiosResponse<Data<PageAdapter<BlogsDesc>>> = await axios.get(`/search/blog?keywords=${queryString}&currentPage=${currentPage}&allInfo=${allInfo}&year=${year}`)
  return Promise.resolve(resp.data.data)
}

let timeout: NodeJS.Timeout
const querySearchAsync = (queryString: string, cb: Function) => {
  if (queryString.length > 0) {
    const resp: Promise<PageAdapter<BlogsDesc>> = query(queryString, -1, false, year.value)
    resp.then((page: PageAdapter<BlogsDesc>) => {
      page.content.forEach((blogsDesc: BlogsDesc) => {
        blogsDesc.value = blogsDesc.highlight
      })
      //节流
      clearTimeout(timeout)
      timeout = setTimeout(() => {
        cb(page.content)
        if (page.content.length === 0) {
          //@ts-ignore
          ElMessage.error('No Records')
        }
      }, 1000 * Math.random())
    })
  }
}

const handleSelect = (item: BlogsDesc) => console.log(item)

const queryAllInfo = async (queryString: string, currentPage = 1) => {
  outerVisible.value = false
  if (queryString.length > 0) {
    const page: PageAdapter<BlogsDesc> = await query(queryString, currentPage, true, year.value)
    keywords.value = queryString
    emit('search', page)
  } else {
    emit('clear')
  }
}

const beforeClose = (close: Function) => {
  keywords.value = ''
  year.value = ''
  refAutocomplete.value.suggestions = []
  emit('clear')
  close()
}

const refAutocomplete: Ref<any> = ref<any>()

const yearClose = async () => {
  innerVisible.value = false
  if (keywords.value.length > 0) {
    const page: PageAdapter<BlogsDesc> = await query(keywords.value, -1, false, year.value)
    page.content.forEach((blogsDesc: BlogsDesc) => {
      blogsDesc.value = blogsDesc.highlight
    })
    //节流
    clearTimeout(timeout)
    timeout = setTimeout(() => {
      refAutocomplete.value.activated = true
      refAutocomplete.value.suggestions = page.content
      if (page.content.length === 0) {
        //@ts-ignore
        ElMessage.error('No Records')
      }
    }, 1000 * Math.random())
  }
}

defineExpose(
  { queryAllInfo }
)
</script>

<template>
  <el-button class="search-button" @click="outerVisible = true" type="success">Search</el-button>
  <el-dialog v-model="outerVisible" center close-on-press-escape fullscreen align-center :before-close="beforeClose">
    <template #default>
      <div class="dialog-content">
        <Hot></Hot>
        <div v-if="year.length > 0">年份：{{ year }}</div>
        <el-autocomplete v-model="keywords" :fetch-suggestions="querySearchAsync" placeholder="Please input"
          @select="handleSelect" :trigger-on-focus="false" clearable @keyup.enter="queryAllInfo(keywords)"
          ref="refAutocomplete">
          <template #default="{ item }">
            <div class="value" v-if="item.value.title" v-for="title in item.value.title" v-html="'标题：' + title"></div>
            <div class="value" v-if="item.value.description" v-for="description in item.value.description"
              v-html="'摘要：' + description"></div>
            <div class="value" v-if="item.value.content" v-for="content in item.value.content" v-html="'内容：' + content">
            </div>
          </template>
        </el-autocomplete>
      </div>
      <el-dialog v-model="innerVisible" width="50%" append-to-body title="Archieve">
        <Years @close="yearClose"></Years>
      </el-dialog>
    </template>
    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" @click="queryAllInfo(keywords)">Confirm</el-button>
        <el-button type="primary" @click="innerVisible = true">Archieve</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.dialog-content {
  margin: 0 auto;
  max-width: max-content;
}

.search-button {
  position: absolute;
  right: 0;
  z-index: 1;
  top: 15px;
}

.el-overlay-dialog .dialog-content {
  text-align: center;
  margin-top: 20px
}
</style>