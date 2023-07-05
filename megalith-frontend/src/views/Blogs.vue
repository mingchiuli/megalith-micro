<script lang="ts" setup>
import type { BlogsDesc, Data, PageAdapter } from '@/type/entity';
import axios from '../axios';
import { reactive, toRefs, ref } from 'vue';
import type { AxiosResponse } from 'axios';
import Search from '@/components/Search.vue';


const fillSearch = (payload: PageAdapter<BlogsDesc>) => {
  if (payload.content.length > 0) {
    page.content = payload.content
    page.totalElements = payload.totalElements
  } else {
    //@ts-ignore
    ElMessage.error("No Records")
    getPage(1)
  }
}

let page : PageAdapter<BlogsDesc> = reactive({
  "content" : [],
  "totalElements" : 0,
  "pageSize" : 5
})

const getPage = (pageNo : number, year = '') : void => {
  axios.get(`/public/blog/page/${pageNo}?year=${year}`)
    .then((resp : AxiosResponse<Data<PageAdapter<BlogsDesc>>>) => {
      page.content = resp.data.data.content
      page.totalElements = resp.data.data.totalElements
    })
}

getPage(1)
const { content : blogs, totalElements, pageSize } = toRefs(page)
</script>
<template>
  <div class="search-father">
    <Search @search="fillSearch"></Search>
  </div>
  <div class="description">
    <el-timeline>
      <el-timeline-item v-for="blog in blogs" :timestamp="blog.created.replace('T', ' ')" placement="top" :color="'#0bbd87'">
        <p v-if="blog.score">{{"Search Scores:" + blog.score}}</p>
        <el-card shadow="never">
          <el-image :key="blog.link" :src="blog.link" lazy></el-image>
          <h4>{{ blog.title }}</h4>
          <p v-if="!blog.highlight">{{ blog.description }}</p>
          <p v-if="blog.highlight?.title" v-html="blog.highlight.title"></p>
          <p v-if="blog.highlight?.description" v-html="blog.highlight.description"></p>
          <p v-if="blog.highlight?.content" v-html="blog.highlight.content"></p>
        </el-card>
      </el-timeline-item>
    </el-timeline>    
    <el-pagination layout="prev, pager, next" :total="totalElements" :page-size="pageSize" @current-change="getPage"></el-pagination>
  </div>
</template>
<style scoped>
@import '../assets/front.css';

.search-father {
  margin-top: 30px;
  position: relative;
  min-height: 10px;
}

.description {
  margin: 0 auto;
}

.el-timeline :deep(.el-timeline-item__wrapper) {
  padding-left: 5%;
}

.el-pagination {
  width: fit-content;
  margin: 0 auto;
}
.el-timeline {
  padding: 0;
}
</style>