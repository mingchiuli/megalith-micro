<script lang="ts" setup>
import { reactive, ref, nextTick, computed, onUnmounted } from 'vue'
import { GET } from '@/http/http'
import type { BlogExhibit } from '@/type/entity'
import catalogue from '@/components/catalogue-item.vue'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/preview.css'
import { useRoute } from 'vue-router'
import { displayStateStore } from '@/stores/store'
import { storeToRefs } from 'pinia'

const route = useRoute()
const token = route.query.token
const blogId = route.params.id
const loading = ref(true)
const loadingCatalogue = ref(true)
const { showCatalogue } = storeToRefs(displayStateStore())
const affixHeight = computed(() => showCatalogue.value ? '100px' : '0')

const blog = reactive<BlogExhibit>({
  "title": '',
  "description": '',
  "content": '',
  "avatar": '',
  "readCount": 0,
  "nickname": '',
  "created": ''
})

onUnmounted(() => displayStateStore().showCatalogue = false)
const catalogueRef = ref<InstanceType<typeof catalogue>>();

(async () => {
  let data: BlogExhibit
  if (token) {
    data = await GET<BlogExhibit>(`/public/blog/secret/${blogId}?readToken=${token}`)
  } else {
    data = await GET<BlogExhibit>(`/public/blog/info/${blogId}`)
  }
  blog.title = data.title
  document.title = data.title
  blog.avatar = data.avatar
  blog.readCount = data.readCount
  blog.nickname = data.nickname
  blog.created = data.created
  loading.value = false
  blog.content = '>' + data.description + '\n\n' + data.content
  await nextTick()
  //基于一些不知道的原因
  setTimeout(async () => await catalogueRef.value?.render(), 100)
})()
</script>

<template>
  <div class="father">
    <div class="affix">
      <catalogue-item v-if="loadingCatalogue" v-show="showCatalogue" ref="catalogueRef"
        v-model:loadingCatalogue="loadingCatalogue" />
    </div>
  </div>

  <div class="exhibit-content">
    <div class="exhibit-title">{{ blog.title }}</div>
    <el-avatar class="exhibit-avatar" :src="blog.avatar" />
    <el-text class="exhibit-author" size="large">作者: {{ blog.nickname }}</el-text>
    <el-text class="exhibit-time" size="default">{{ blog.created }}</el-text>
    <el-text class="exhibit-read-count" size="default">阅读数: {{ blog.readCount }}</el-text>
    <el-skeleton animated :loading="loading" :throttle="300">
      <template #template>
        <el-skeleton :rows="15" />
      </template>
      <template #default>
        <el-card shadow="never" class="content">
          <md-preview editorId="preview-only" v-model="blog.content" :showCodeRowNumber="true" />
        </el-card>
      </template>
    </el-skeleton>
    <discuss-item />
  </div>
  <my-footer-item />
</template>

<style scoped>
.exhibit-content {
  max-width: 40rem;
  margin: 0 auto;
  padding: .5rem
}

.exhibit-title {
  text-align: center;
  font-size: xx-large;
  margin-top: 30px;
  margin-bottom: 20px
}

.exhibit-mavon-editor {
  padding: 20px
}

.exhibit-avatar {
  margin: 0 auto;
  display: block
}

.exhibit-author {
  display: block;
  text-align: center;
  margin-top: 5px
}

.exhibit-time {
  display: block;
  margin-left: 10px
}

.exhibit-read-count {
  display: block;
  margin-left: 10px
}

.el-card:deep(.md-editor-preview-wrapper) {
  padding: 20px 20px
}

.content:deep(.el-card__body) {
  padding: 0
}

.affix {
  right: 10%;
  position: fixed;
  margin-top: 30px;
  display: block
}

.father {
  height: v-bind(affixHeight)
}
</style>