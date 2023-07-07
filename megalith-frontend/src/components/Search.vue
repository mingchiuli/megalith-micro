<script lang="ts" setup>
import type { BlogsDesc, Data, PageAdapter } from '@/type/entity'
import axios from '../axios'
import { ref, type Ref } from 'vue'
import type { AxiosResponse } from 'axios'

const emit = defineEmits<{
  (event: "search", payload: PageAdapter<BlogsDesc>, keywords: string): void
  (event: "clear"): void
  (event: "transYear", payload: string): void
}>()

const keywords: Ref<string> = ref('')
const year : Ref<string>= ref('')

const outerVisible: Ref<boolean> = ref(false)
const innerVisible: Ref<boolean> = ref(false)

const query: Function = async (queryString: string, currentPage: number, allInfo: boolean, year: string): Promise<PageAdapter<BlogsDesc>> => {
  const resp : AxiosResponse<Data<PageAdapter<BlogsDesc>>> = await axios.get(`/search/blog?keywords=${queryString}&currentPage=${currentPage}&allInfo=${allInfo}&year=${year}`)
  return Promise.resolve(resp.data.data)
}

let timeout: NodeJS.Timeout
const querySearchAsync: Function = (queryString: string, cb: any) => {
  const resp: Promise<PageAdapter<BlogsDesc>> =  query(queryString, -1, false, year.value)
  resp.then((page: PageAdapter<BlogsDesc>) => {
    page.content.forEach((blogsDesc: BlogsDesc) => {
      blogsDesc.value = blogsDesc.highlight
    })
    //节流
    clearTimeout(timeout)
    timeout = setTimeout(() => {
      if (page.content.length > 0) {
        cb(page.content)
      } else {
        //@ts-ignore
        ElMessage.error("No Records")
      }
    }, 1000 * Math.random())
  })
}
  
const handleSelect = (item: BlogsDesc) => console.log(item)

const queryAllInfo: Function = (queryString: string, currentPage = 1) => {
  outerVisible.value = false
  if (queryString !== '') {
    const resp: Promise<PageAdapter<BlogsDesc>> = query(queryString, currentPage, true, year.value)
    resp.then((page: PageAdapter<BlogsDesc>) => {
      emit("search" , page, queryString)
    })
  } else {
    emit("clear")
  }
}

const beforeClose: Function = (close: Function) => {
  keywords.value = ''
  year.value = ''
  emit('clear')
  close()
}

defineExpose({queryAllInfo})
</script>

<template>
  <el-button class="search-button" @click="outerVisible = true" type="success">Search</el-button>
  <el-dialog v-model="outerVisible" 
  center 
  close-on-press-escape 
  fullscreen 
  align-center
  :before-close="beforeClose">
    <template #default>
      <div class="dialog-content">
        <el-autocomplete v-model="keywords" 
        :fetch-suggestions="querySearchAsync" 
        placeholder="Please input" 
        @select="handleSelect"
        :trigger-on-focus="false"
        clearable
        @keyup.enter="queryAllInfo(keywords)">
        <template #default="{ item }">          
          <div class="value" v-if="item.value.title" v-for="title in item.value.title" v-html="'标题：' + title"></div>
          <div class="value" v-if="item.value.description" v-for="description in item.value.description" v-html="'摘要：' + description"></div>
          <div class="value" v-if="item.value.content" v-for="content in item.value.content" v-html="'内容：' + content"></div>
        </template>
      </el-autocomplete>
      </div>
      <el-dialog v-model="innerVisible" width="50%" append-to-body title="Archieve">aaa</el-dialog>
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
}

.el-overlay-dialog .dialog-content {
  margin-top: 50px
}

</style>