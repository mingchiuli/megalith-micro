<script lang="ts" setup>
import type { BlogDesc, PageAdapter } from '@/type/entity'
import { GET } from '@/http/http'
import { reactive, toRefs, ref } from 'vue'
import { loginStateStore, tabStore, blogsPageNumStore } from '@/stores/store'
import router from '@/router'
import { storeToRefs } from 'pinia'
import search from '@/components/search-item.vue'
import { Status } from '@/type/entity'

const loading = ref(true)
const loginDialogVisible = ref(false)
const searchDialogVisible = ref(false)
const searchRef = ref<InstanceType<typeof search>>()
const year = ref('')
const keywords = ref('')
const readTokenDialogVisible = ref(false)
const blogId = ref(0)

const page: PageAdapter<BlogDesc> = reactive({
  "content": [],
  "totalElements": 0,
  "pageSize": 5,
  //不用这个字段
  "pageNumber": undefined
})  
//用这个字段
const { pageNum } = storeToRefs(blogsPageNumStore())
const { login } = storeToRefs(loginStateStore())

if (router.currentRoute.value.path === '/login' && !login.value) {
  loginDialogVisible.value = true
} else {
  router.push({
    name: 'blogs'
  })
}

const fillSearchData = (payload: PageAdapter<BlogDesc>) => {
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
  const data = await GET<PageAdapter<BlogDesc>>(`/public/blog/page/${pageNo}?year=${year}`)
  page.content = data.content
  page.totalElements = data.totalElements
  loading.value = false
}

const getPage = async (pageNo: number) => {
  pageNum.value = pageNo
  if (!keywords.value) {
    await queryBlogs(pageNo, year.value)
  } else {
    searchRef.value!.searchAllInfo(keywords.value, pageNo)
  }
}

const to = async (id: number) => {
  const status = await GET<number>(`/public/blog/status/${id}`)
  if (status === Status.NORMAL) {
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

const { content, totalElements, pageSize } = toRefs(page);

(async () => {
  await getPage(pageNum.value)
  loading.value = false
})()
</script>

<template>
  <div class="front">
    <blog-login-item v-model:loginDialogVisible="loginDialogVisible"></blog-login-item>
    <read-token-item v-model:readTokenDialogVisible="readTokenDialogVisible" v-model:blogId="blogId"></read-token-item>
    <div class="search-father">
      <el-button class="search-button" @click="searchDialogVisible = true" type="success">Search</el-button>
      <search-item ref="searchRef" @transSearchData="fillSearchData" @clear="getPage(1)" v-model:keywords="keywords"
        v-model:year="year" v-model:loading="loading" v-model:searchDialogVisible="searchDialogVisible"></search-item>
    </div>
    <el-text size="large">共{{ page.totalElements }}篇</el-text>
    <el-link type="success" size="large" class="door" v-if="login"
      @click="router.push({ name: tabStore().editableTabsValue ? tabStore().editableTabsValue : 'system' })">进入后台</el-link>
    <br />
    <div class="description">
      <el-timeline>
        <el-skeleton animated :loading="loading" :throttle="300">
          <template #template>
            <el-skeleton v-for="i in page.pageSize" v-bind:key="i" :rows="5" animated />
          </template>
          <template #default>
            <el-timeline-item v-for="blog in content" v-bind:key="blog.id" :timestamp="blog.created" placement="top"
              :color="'#0bbd87'">
              <el-card shadow="never">
                <el-image v-if="blog.link" :key="blog.link" :src="blog.link" lazy></el-image>
                <p v-if="blog.score">{{ "Search Scores: " + blog.score }}</p>
                <el-link class="title" @click="to(blog.id)">{{ blog.title }}</el-link>
                <p v-if="!blog.highlight">{{ blog.description }}</p>
                <template v-if="blog.highlight?.title">
                  <p v-for="(title, key) in blog.highlight.title" v-bind:key="key" v-html="'标题: ' + title"></p>
                </template>
                <template v-if="blog.highlight?.description">
                  <p v-for="(description, key) in blog.highlight.description" v-bind:key="key" v-html="'摘要: ' + description"></p>
                </template>
                <template v-if="blog.highlight?.content">
                  <p v-for="(content, key) in blog.highlight.content" v-bind:key="key" v-html="'内容: ' + content"></p>
                </template>
              </el-card>
            </el-timeline-item>
          </template>
        </el-skeleton>
      </el-timeline>
      <el-pagination layout="prev, pager, next" :total="totalElements" :page-size="pageSize" @current-change="getPage" :current-page="pageNum" />
    </div>
  </div>
  <my-footer-item />
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
  display: block;
  max-width: fit-content;
}

.search-button {
  position: absolute;
  right: 0;
  z-index: 1;
  top: 15px;
}
</style>