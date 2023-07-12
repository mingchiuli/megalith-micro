<script lang="ts" setup>
import { ref, type Ref } from 'vue';
import type { Hot, Data } from '@/type/entity'
import { type AxiosResponse } from 'axios'
import axios from '@/axios';
import router from '@/router';

let hots: Ref<Hot[]> = ref([])

axios.get('/public/blog/scores')
  .then((resp: AxiosResponse<Data<Hot[]>>) => {
    hots.value = resp.data.data
  })

const go = (id: number) => router.push({
  name: 'blog',
  query: {
    blogId: id
  }
})

</script>

<template>
  <el-card shadow="never" class="hot-blogs">
    <div class="title">本周热读</div>
    <el-link class="description" v-for="hot in hots" @click="go(hot.id)">{{ hot.title }}: {{ hot.readCount }}</el-link>
  </el-card>
</template>

<style scoped>
.hot-blogs {
  max-width: 250px;
  margin-bottom: 20px
}

.title {
  text-align: center;
  margin-bottom: 20px
}

.description {
  text-align: left;
  margin-bottom: 10px
}
</style>