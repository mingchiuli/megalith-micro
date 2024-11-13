<script lang="ts" setup>
import { nextTick, onMounted, onUnmounted, reactive, ref, useTemplateRef } from 'vue'
import { GET } from '@/http/http'
import type { BlogExhibit } from '@/type/entity'
import Catalogue from '@/components/CatalogueItem.vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const token = route.query.token
const blogId = route.params.id
const loading = ref(true)
const loadingCatalogue = ref(true)
const affixHeight = ref(document.body.clientWidth > 900 ? '100px' : '0')
const showCatalogue = ref(false)
const catalogueWidth = ref(200)
const right = ref(10)

const blog = reactive<BlogExhibit>({
  title: '',
  description: '',
  content: '',
  avatar: '',
  readCount: 0,
  nickname: '',
  created: ''
})

const catalogueRef = useTemplateRef<InstanceType<typeof Catalogue>>('catalogue')

let renderCatalogueCount = 0
const renderCatalogue = async (html: string) => {
  if (html && !renderCatalogueCount) {
    loading.value = false
    await nextTick()
    computeWidth()
    renderCatalogueCount++
    await catalogueRef.value?.render()
  }
}

const computeWidth = () => {
  //计算距离
  const screenWidth = window.outerWidth
  const label = document.querySelector<HTMLElement>('.content')
  const width = (screenWidth - label!.offsetWidth) / 2

  const halfWidth = width / 2
  if (halfWidth > catalogueWidth.value + 10) {
    right.value = halfWidth
    showCatalogue.value = true
  } else if (width > catalogueWidth.value + 110) {
    right.value = 100
    showCatalogue.value = true
  } else if (width > catalogueWidth.value + 20) {
    right.value = 10
    showCatalogue.value = true
  } else {
    showCatalogue.value = false
  }
}

onMounted(() => window.addEventListener('resize', computeWidth))
onUnmounted(() => window.removeEventListener('resize', computeWidth))
;(async () => {
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
  blog.content = '>' + data.description + '\n\n' + data.content
})()
</script>

<template>
  <div class="father">
    <div class="affix">
      <CatalogueItem
        v-if="loadingCatalogue"
        v-show="showCatalogue"
        ref="catalogue"
        v-model:loading-catalogue="loadingCatalogue"
        :width="catalogueWidth"
      />
    </div>
  </div>

  <div class="front">
    <div class="exhibit-title">{{ blog.title }}</div>
    <el-avatar class="exhibit-avatar" :src="blog.avatar" />
    <el-text class="exhibit-author" size="large">作者: {{ blog.nickname }}</el-text>
    <el-text class="exhibit-time" size="default">{{ blog.created }}</el-text>
    <el-text class="exhibit-read-count" size="default">阅读数: {{ blog.readCount }}</el-text>
    <el-skeleton animated :loading="loading" :throttle="300">
      <template #template>
        <el-skeleton :rows="15" />
      </template>
    </el-skeleton>
    <el-card shadow="never" class="content" v-show="!loading">
      <md-preview
        id="preview-only"
        v-model="blog.content"
        :showCodeRowNumber="true"
        @on-html-changed="renderCatalogue"
      />
    </el-card>
    <DiscussItem />
  </div>
</template>

<style scoped>
@import '@/assets/front.css';

.exhibit-title {
  text-align: center;
  font-size: xx-large;
  margin-top: 30px;
  margin-bottom: 20px;
}

.exhibit-mavon-editor {
  padding: 20px;
}

.exhibit-avatar {
  margin: 0 auto;
  display: block;
}

.exhibit-author {
  display: block;
  text-align: center;
  margin-top: 5px;
}

.exhibit-time {
  margin-top: 10px;
  display: block;
  margin-left: 10px;
}

.exhibit-read-count {
  display: block;
  margin-left: 10px;
}

.el-card:deep(.md-editor-preview-wrapper) {
  padding: 20px 20px;
}

.content:deep(.el-card__body) {
  padding: 0;
}

.affix {
  right: v-bind(right + 'px');
  position: fixed;
  margin-top: 30px;
  display: block;
}

.father {
  height: v-bind(affixHeight);
}
</style>
