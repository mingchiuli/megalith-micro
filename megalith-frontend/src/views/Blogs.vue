<script lang="ts" setup>
import type { BlogsDesc, PageAdapter } from '@/type/entity'
import { GET } from '@/http/http'
import { reactive, toRefs, ref, type Ref } from 'vue'
import { storeToRefs } from 'pinia'
import { loginStateStore } from '@/stores/store'
import router from '@/router'

const loading: Ref<boolean> = ref(true)
const loginDialog: Ref<boolean> = ref(false)
const { login } = storeToRefs(loginStateStore())
const searchRef: Ref<any> = ref()
const searchPageNo: Ref<number> = ref(0)
const year: Ref<string> = ref('')
const keywords: Ref<string> = ref('')
const readTokenDialog: Ref<boolean> = ref(false)
const blogId: Ref<number> = ref(0)


let page: PageAdapter<BlogsDesc> = reactive({
  "content": [],
  "totalElements": 0,
  "pageSize": 5,
  "pageNumber": 1
})

if (router.currentRoute.value.path === '/login' && !login.value) {
  loginDialog.value = true
} else {
  router.push({
    name: 'blogs'
  })
}

const fillSearchData = (payload: PageAdapter<BlogsDesc>) => {
  if (payload.content.length > 0) {
    page.content = payload.content
    page.totalElements = payload.totalElements
    searchPageNo.value = payload.pageNumber
    loading.value = false
  } else {
    queryBlogs(1, '')
  }
}

const queryBlogs = async (pageNo: number, year: string) => {
  loading.value = true
  const data = await GET<PageAdapter<BlogsDesc>>(`/public/blog/page/${pageNo}?year=${year}`)
  page.content = data.content
  page.totalElements = data.totalElements
  loading.value = false
}

const getPage = async (pageNo: number) => {
  if (searchPageNo.value === 0) {
    queryBlogs(pageNo, year.value)
  } else {
    searchRef.value.queryAllInfo(keywords.value, pageNo)
  }
}

const clear = () => {
  searchPageNo.value = 0
  getPage(1)
}

const go = async (id: number) => {
  const status = await GET<number>(`/public/blog/status/${id}`)
  if (status === 0) {
    router.push({
      name: 'blog',
      params: {
        id: id
      }
    })
  } else {
    readTokenDialog.value = true
    blogId.value = id
  }
}

const { content: blogs, totalElements, pageSize } = toRefs(page);

(async () => {
  await getPage(1)
  loading.value = false
})()
</script>

<template>
  <div class="front">
    <Login v-model:loginDialog="loginDialog"></Login>
    <ReadToken v-model:readTokenDialog="readTokenDialog" v-model:blogId="blogId"></ReadToken>
    <div class="search-father">
      <Search ref="searchRef" @transSearchData="fillSearchData" @clear="clear" v-model:keywords="keywords"
        v-model:year="year" v-model:loading="loading"></Search>
    </div>
    <el-text size="large">共{{ page.totalElements }}篇</el-text>
    <br />
    <div class="description">
      <el-timeline>
        <el-skeleton animated :loading="loading" :throttle="300">
          <template #template>
            <el-skeleton v-for=" in page.pageSize" :rows="5" :loading="loading" animated />
          </template>
          <template #default>
            <el-timeline-item v-for="blog in blogs" :timestamp="blog.created.replace('T', ' ')" placement="top"
              :color="'#0bbd87'">
              <el-card shadow="never">
                <el-image :key="blog.link" :src="blog.link" lazy></el-image>
                <p v-if="blog.score">{{ "Search Scores:" + blog.score }}</p>
                <el-link class="title" @click="go(blog.id)">{{ blog.title }}</el-link>
                <p v-if="!blog.highlight">{{ blog.description }}</p>
                <p v-if="blog.highlight?.title" v-for="title in blog.highlight.title" v-html="'标题：' + title"></p>
                <p v-if="blog.highlight?.description" v-for="description in blog.highlight.description"
                  v-html="'摘要：' + description"></p>
                <p v-if="blog.highlight?.content" v-for="content in blog.highlight.content" v-html="'内容：' + content"></p>
              </el-card>
            </el-timeline-item>
          </template>
        </el-skeleton>
      </el-timeline>
      <el-pagination layout="prev, pager, next" :total="totalElements" :page-size="pageSize"
        @current-change="getPage"></el-pagination>
    </div>
  </div>
</template>

<style scoped>

.search-father {
  margin-top: 20px;
  position: relative;
  min-height: 10px;
}

.description {
  margin: 0 auto;
}

.el-timeline:deep(.el-timeline-item__wrapper) {
  padding-left: 5%;
}

.el-pagination {
  width: fit-content;
  margin: 0 auto;
}

.el-timeline {
  padding: 0;
}

.title {
  font-size: medium;
  margin-top: 15px;
}
</style>