<script lang="ts" setup>
import type { BlogDesc, PageAdapter } from '@/type/entity'
import { GET } from '@/http/http'
import { reactive, toRefs, ref, nextTick, useTemplateRef } from 'vue'
import { loginStateStore, tabStore, blogsStore } from '@/stores/store'
import router from '@/router'
import { storeToRefs } from 'pinia'
import Search from '@/components/SearchItem.vue'
import { Status } from '@/type/entity'

const loading = ref(true)
const searchDialogVisible = ref(false)
const searchRef = useTemplateRef<InstanceType<typeof Search>>('search')
const readTokenDialogVisible = ref(false)
const blogId = ref(0)

const page: PageAdapter<BlogDesc> = reactive({
  content: [],
  totalElements: 0,
  pageSize: 5,
  //不用这个字段
  pageNumber: undefined
})
//用这个字段
const { searchPageNum } = storeToRefs(blogsStore())
const { pageNum } = storeToRefs(blogsStore())
const { keywords } = storeToRefs(blogsStore())
const { year } = storeToRefs(blogsStore())

const { login } = storeToRefs(loginStateStore())

const fillSearchData = (payload: PageAdapter<BlogDesc>) => {
  initImgCount()
  if (payload.content.length) {
    statImg(payload.content)
    if (!imgCount) loading.value = false
    content.value = payload.content
    totalElements.value = payload.totalElements
    loading.value = false
  } else {
    queryBlogs(1, '')
  }
}

const clearSearch = () => {
  keywords.value = ''
  refresh()
}

const refresh = () => {
  page.content = []
  page.totalElements = 0
  getPage(1)
}

const queryBlogs = async (pageNo: number, year: string) => {
  loading.value = true
  const data = await GET<PageAdapter<BlogDesc>>(`/public/blog/page/${pageNo}?year=${year}`)
  statImg(data.content)
  if (!imgCount) loading.value = false
  page.content = data.content
  page.totalElements = data.totalElements
}

const statImg = (items: BlogDesc[]) => {
  items.forEach((item) => {
    if (item.link) imgCount++
  })
}

const getPage = async (pageNo: number) => {
  initImgCount()
  if (!keywords.value) {
    pageNum.value = pageNo
    await queryBlogs(pageNo, year.value)
  } else {
    searchPageNum.value = pageNo
    await nextTick()
    searchRef.value!.searchAllInfo(keywords.value, pageNo)
  }
}

const initImgCount = () => {
  imgCount = 0
  count = 0
}

let count = 0
let imgCount = 0
const loadImg = () => {
  count++
  if (imgCount <= count) {
    loading.value = false
  }
}

const search = () => {
  searchDialogVisible.value = true
}

const to = async (id: number) => {
  const status = await GET<number>(`/public/blog/status/${id}`)
  if (status === Status.NORMAL || status === Status.SENSITIVE_FILTER) {
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

const { content, totalElements, pageSize } = toRefs(page)

;(async () => {
  await getPage(keywords.value ? searchPageNum.value : pageNum.value)
})()
</script>

<template>
  <div class="front">
    <ReadTokenItem
      v-model:read-token-dialog-visible="readTokenDialogVisible"
      v-model:blog-id="blogId"
    />
    <div class="search-father">
      <el-button class="search-button" @click="search" type="success">Search</el-button>
      <SearchItem
        ref="search"
        @trans-search-data="fillSearchData"
        @refresh="refresh"
        v-model:keywords="keywords"
        v-model:year="year"
        v-model:loading="loading"
        v-model:search-dialog-visible="searchDialogVisible"
      />
    </div>
    <el-text size="large">共{{ page.totalElements }}篇</el-text>
    <el-link
      type="success"
      size="large"
      class="door"
      v-if="login"
      @click="
        router.push({
          name: tabStore().editableTabsValue ? tabStore().editableTabsValue : 'system'
        })
      "
      >进入后台</el-link
    >
    <el-link class="door" type="warning" v-if="keywords" link @click="clearSearch">返回</el-link>
    <br />
    <div class="description">
      <el-skeleton animated :loading="loading" :throttle="300">
        <template #template>
          <el-skeleton v-for="i in page.pageSize" v-bind:key="i" :rows="10" animated />
        </template>
      </el-skeleton>
      <el-timeline>
        <el-timeline-item
          v-for="blog in content"
          v-bind:key="blog.id"
          :timestamp="blog.created"
          placement="top"
          :color="'#0bbd87'"
          v-show="!loading"
        >
          <el-card shadow="hover" @click="to(blog.id)">
            <el-image
              v-if="blog.link"
              :key="blog.link"
              :src="blog.link"
              @load="loadImg"
              @error="loading = false"
            ></el-image>
            <p v-if="blog.score">{{ 'Search Scores: ' + blog.score }}</p>
            <el-link class="title">{{ blog.title }}</el-link>
            <p v-if="!blog.highlight">{{ blog.description }}</p>
            <template v-if="blog.highlight?.title">
              <p
                v-for="(title, key) in blog.highlight.title"
                v-bind:key="key"
                v-html="'标题: ' + title"
              ></p>
            </template>
            <template v-if="blog.highlight?.description">
              <p
                v-for="(description, key) in blog.highlight.description"
                v-bind:key="key"
                v-html="'摘要: ' + description"
              ></p>
            </template>
            <template v-if="blog.highlight?.content">
              <p
                v-for="(content, key) in blog.highlight.content"
                v-bind:key="key"
                v-html="'内容: ' + content"
              ></p>
            </template>
          </el-card>
        </el-timeline-item>
      </el-timeline>
      <el-pagination
        layout="prev, pager, next"
        :total="totalElements"
        :page-size="pageSize"
        @current-change="getPage"
        :current-page="keywords ? searchPageNum : pageNum"
      />
    </div>
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
  margin: 0;
}
</style>
