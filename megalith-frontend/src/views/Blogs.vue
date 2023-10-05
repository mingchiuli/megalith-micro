<script lang="ts" setup>
import type { BlogsDesc, PageAdapter } from '@/type/entity'
import { GET } from '@/http/http'
import { reactive, toRefs, ref } from 'vue'
import { loginStateStore, tabStore } from '@/stores/store'
import router from '@/router'
import { storeToRefs } from 'pinia'
import Search from '@/components/Search.vue'

const loading = ref(true)
const loginDialogVisible = ref(false)
const searchDialogVisible = ref(false)
const searchRef = ref<InstanceType<typeof Search>>()
const year = ref('')
const keywords = ref('')
const readTokenDialogVisible = ref(false)
const blogId = ref(0)

let page: PageAdapter<BlogsDesc> = reactive({
  "content": [],
  "totalElements": 0,
  "pageSize": 5,
  "pageNumber": 1
})

const { login } = storeToRefs(loginStateStore())

if (router.currentRoute.value.path === '/login' && !login.value) {
  loginDialogVisible.value = true
} else {
  router.push({
    name: 'blogs'
  })
}

const fillSearchData = (payload: PageAdapter<BlogsDesc>) => {
  if (payload.content.length) {
    content.value = payload.content
    totalElements.value = payload.totalElements
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
  pageNumber.value = pageNo
  if (!keywords.value) {
    queryBlogs(pageNumber.value, year.value)
  } else {
    searchRef.value?.searchAllInfo(keywords.value, pageNumber.value)
  }
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
    readTokenDialogVisible.value = true
    blogId.value = id
  }
}

const { content, totalElements, pageSize, pageNumber } = toRefs(page);

(async () => {
  await getPage(1)
  loading.value = false
})()
</script>

<template>
  <div class="front">
    <Login v-model:loginDialogVisible="loginDialogVisible"></Login>
    <ReadToken v-model:readTokenDialogVisible="readTokenDialogVisible" v-model:blogId="blogId"></ReadToken>
    <div class="search-father">
      <el-button class="search-button" @click="searchDialogVisible = true" type="success">Search</el-button>
      <Search ref="searchRef" @transSearchData="fillSearchData" @clear="getPage(1)" v-model:keywords="keywords"
        v-model:year="year" v-model:loading="loading" v-model:searchDialogVisible="searchDialogVisible"></Search>
    </div>
    <el-text size="large">共{{ page.totalElements }}篇</el-text>
    <el-link type="success" size="large" class="door" v-if="login" @click="router.push({name: tabStore().editableTabsValue ? tabStore().editableTabsValue : 'system'})">进入后台</el-link>
    <br />
    <div class="description">
      <el-timeline>
        <el-skeleton animated :loading="loading" :throttle="300">
          <template #template>
            <el-skeleton v-for=" in page.pageSize" :rows="5" :loading="loading" animated />
          </template>
          <template #default>
            <el-timeline-item v-for="blog in content" :timestamp="blog.created" placement="top"
              :color="'#0bbd87'">
              <el-card shadow="never">
                <el-image v-if="blog.link" :key="blog.link" :src="blog.link" lazy></el-image>
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
  <Footer />
</template>

<style scoped>

.door {
  width: fit-content;
  margin-left: 5px;
}

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

.search-button {
  position: absolute;
  right: 0;
  z-index: 1;
  top: 15px;
}
</style>