<script lang="ts" setup>
import { onErrorCaptured, reactive } from 'vue';
import { useRoute, type RouteLocationNormalizedLoaded } from 'vue-router';
import { GET } from '@/http/http'
import type { BlogExhibit, Data } from '@/type/entity';
import { markdown } from '@/utils/markdown'
import editor from 'mavon-editor';


const router: RouteLocationNormalizedLoaded = useRoute();
const token = router.query.token
const blogId = router.params.id

let blog: BlogExhibit = reactive({
  "title": '',
  "description": '',
  "content": '',
  "avatar": '',
  "readCount": 0,
  "nickname": 'Anonymous',
  "created": ''
});

onErrorCaptured((err, instance, info): boolean => {
  if (info === 'beforeUnmount hook') {
    return false
  }
  return true
});


(async () => {
  let data: Data<BlogExhibit>;
  if (token) {
    data = await GET<BlogExhibit>(`/public/blog/secret/${blogId}`)
  } else {
    data = await GET<BlogExhibit>(`/public/blog/info/${blogId}`)
  }
  blog.title = data.data.title
  blog.content = '<blockquote> <p>' + data.data.description +  '</p> </blockquote>' + markdown(editor.mavonEditor, data.data.content)
  blog.avatar = data.data.avatar
  blog.readCount = data.data.readCount
  blog.nickname = data.data.nickname
  blog.created = data.data.created
})()
</script>

<template>
  <div class="exhibit-title">{{ blog.title }}</div>
  <el-avatar class="exhibit-avatar" :src="blog.avatar" />
  <el-text class="exhibit-author" size="large">{{ blog.nickname }}</el-text>
  <mavon-editor class="exhibit-mavon-editor" :boxShadow="false" :editable="false" :subfield="false" v-html="blog.content"
    :toolbarsFlag="false" defaultOpen="preview" previewBackground="#ffffff" code-style="androidstudio" />
</template>

<style lang="less">
@import '../assets/hljs.less';

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
  margin-bottom: 15px;
}
</style>