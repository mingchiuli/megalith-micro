<script lang="ts" setup>
import { ref, type Ref } from 'vue'
import type { Hot } from '@/type/entity'
import { GET } from '@/http/http'
import router from '@/router'

let hots: Ref<Hot[]> = ref([])
const data = await GET<Hot[]>('/public/blog/scores')
hots.value = data.data

const go = (id: number) => router.push({
  name: 'blog',
  query: {
    blogId: id
  }
})
</script>

<template>
  <el-card shadow="never" class="hot-blogs">
    <div class="title">
      <el-text>本周热读</el-text>
    </div>
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