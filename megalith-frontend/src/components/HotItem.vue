<script lang="ts" setup>
import { ref } from 'vue'
import type { Hot } from '@/type/entity'
import { GET } from '@/http/http'
import router from '@/router'

const hots = ref<Hot[]>()
const loading = ref(false)

const to = (id: number) =>
  router.push({
    name: 'blog',
    params: {
      id: id
    }
  })

const load = async () => {
  loading.value = true
  hots.value = await GET<Hot[]>('/public/blog/scores')
  loading.value = false
}

defineExpose({ load })
</script>

<template>
  <el-card shadow="never" class="hot-blogs" v-loading="loading">
    <div class="title">
      <el-text>本周阅读排行</el-text>
    </div>
    <div class="description" v-for="(hot, key) in hots" v-bind:key="key">
      <el-link @click="to(hot.id)"
        >{{ hot.title ? hot.title : '匿名文章' }}: {{ hot.readCount }}</el-link
      >
      <br />
    </div>
  </el-card>
</template>

<style scoped>
.hot-blogs {
  max-width: 250px;
  margin-bottom: 20px;
}

.title {
  text-align: center;
  margin-bottom: 20px;
}

.description {
  text-align: left;
  margin-bottom: 10px;
}
</style>
