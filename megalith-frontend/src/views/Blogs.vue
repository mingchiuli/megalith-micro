<script lang="ts" setup>
import type { Data, PageAdapter } from '@/type/entity';
import axios from '../axios';
import { reactive, toRefs } from 'vue';
import type { AxiosResponse } from 'axios';

let page : PageAdapter = reactive({
  "content" : [],
  "totalElements" : 0,
  "pageSize" : 5
})

const getPage = (pageNo : number) : void => {
  axios.get(`/public/blog/page/${pageNo}`)
    .then((resp : AxiosResponse<Data<PageAdapter>>) => {
      page.content = resp.data.data.content
      page.totalElements = resp.data.data.totalElements
    })
}

getPage(1)
const { content : blogs, totalElements, pageSize } = toRefs(page)
</script>
<template>
  <div class="description">
    <el-timeline>
      <el-timeline-item v-for="blog in blogs" :timestamp="blog.created.replace('T', ' ')" placement="top" :color="'#0bbd87'">
        <el-card shadow="never">
          <el-image :key="blog.link" :src="blog.link" lazy></el-image>
          <h4>{{ blog.title }}</h4>
          <p>{{ blog.description }}</p>
        </el-card>
      </el-timeline-item>
    </el-timeline>
    <el-pagination layout="prev, pager, next" :total="totalElements" :page-size="pageSize" @current-change="getPage"></el-pagination>
  </div>
</template>
<style scoped>
@import '../assets/front.css';
.description {
  max-width: 600px;
  margin: 0 auto;
  margin-top: 50px;
}

.el-pagination {
  width: max-content;
  margin: 0 auto;
}
.el-timeline {
  padding: 0;
}
</style>