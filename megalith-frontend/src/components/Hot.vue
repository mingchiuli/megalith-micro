<script lang="ts" setup>
import { ref } from 'vue'
import type { Hot } from '@/type/entity'
import { GET } from '@/http/http'
import router from '@/router'

let hots = ref<Hot[]>()

const to = (id: number) => router.push({
  name: 'blog',
  params: {
    id: id
  }
});

(async () => {
  hots.value = await GET<Hot[]>('/public/blog/scores')
})()

</script>

<template>
  <el-card shadow="never" class="hot-blogs">
    <div class="title">
      <el-text>本周热读</el-text>
    </div>
    <div class="description" v-for="hot in hots" >
      <el-link @click="to(hot.id)">{{ hot.title }}: {{ hot.readCount }}</el-link>
      <br/>
    </div>
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
  margin-bottom: 10px;
}
</style>