<script lang="ts" setup>
import type { BlogsDesc, Data, PageAdapter } from '@/type/entity'
import axios from '@/axios'
import { reactive, toRefs, ref, type Ref } from 'vue'
import type { AxiosResponse } from 'axios'
import { storeToRefs } from 'pinia'
import { searchStore } from '@/stores/store'
import router from '@/router'

const searchRef: Ref<any> = ref<any>()
const searchPageNo: Ref<number> = ref(0)

const { keywords, year } = storeToRefs(searchStore())

const fillSearch = (payload: PageAdapter<BlogsDesc>) => {
  if (payload.content.length > 0) {
    page.content = payload.content
    page.totalElements = payload.totalElements
    searchPageNo.value = payload.pageNumber
  }
}

let page: PageAdapter<BlogsDesc> = reactive({
  "content": [],
  "totalElements": 0,
  "pageSize": 5,
  "pageNumber": 1
})

const getPage = (pageNo: number): void => {
  if (searchPageNo.value === 0) {
    axios.get(`/public/blog/page/${pageNo}?year=${year.value}`)
      .then((resp: AxiosResponse<Data<PageAdapter<BlogsDesc>>>) => {
        page.content = resp.data.data.content
        page.totalElements = resp.data.data.totalElements
      })
  } else {
    searchRef.value.queryAllInfo(keywords.value, pageNo)
  }
}

const clear = () => {
  searchPageNo.value = 0
  getPage(1)
}

const go = () => router.push({
  name: 'blog'
})

const { content: blogs, totalElements, pageSize } = toRefs(page)

getPage(1)
</script>

<template>
  <Login></Login>
  <div class="search-father">
    <Search ref="searchRef" @search="fillSearch" @clear="clear"></Search>
  </div>
  <div>共{{ page.totalElements }}篇</div>
  <br />
  <div class="description">
    <el-timeline>
      <el-timeline-item v-for="blog in blogs" :timestamp="blog.created.replace('T', ' ')" placement="top"
        :color="'#0bbd87'">
        <el-card shadow="never">
          <el-image :key="blog.link" :src="blog.link" lazy></el-image>
          <p v-if="blog.score">{{ "Search Scores:" + blog.score }}</p>
          <el-link class="title" @click="go">{{ blog.title }}</el-link>
          <p v-if="!blog.highlight">{{ blog.description }}</p>
          <p v-if="blog.highlight?.title" v-for="title in blog.highlight.title" v-html="'标题：' + title"></p>
          <p v-if="blog.highlight?.description" v-for="description in blog.highlight.description"
            v-html="'摘要：' + description"></p>
          <p v-if="blog.highlight?.content" v-for="content in blog.highlight.content" v-html="'内容：' + content"></p>
        </el-card>
      </el-timeline-item>
    </el-timeline>
    <el-pagination layout="prev, pager, next" :total="totalElements" :page-size="pageSize"
      @current-change="getPage"></el-pagination>
  </div>
</template>

<style scoped>
@import '../assets/front.css';

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
}</style>