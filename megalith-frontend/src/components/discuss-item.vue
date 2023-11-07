<script lang="ts" setup>
import DisqusJS from 'disqusjs/dist/disqusjs.es2015.es.js'
import 'disqusjs/dist/styles/disqusjs.css'
import { onMounted, ref } from 'vue'

const activeName = ref('first')

onMounted(() => {
  const utterances = document.createElement('script')
  utterances.type = 'text/javascript'
  utterances.async = true
  utterances.setAttribute('issue-term', 'pathname')
  utterances.setAttribute('theme', 'github-light')
  utterances.setAttribute('repo', 'mingchiuli/utterancesDemo')
  utterances.crossOrigin = 'anonymous'
  utterances.src = 'https://utteranc.es/client.js'

  const disqus = new DisqusJS({
    shortname: '81-68-192-120',
    siteName: 'mingchiuli',
    identifier: document.location.origin + document.location.pathname + document.location.search,
    url: document.location.origin + document.location.pathname + document.location.search,
    title: document.title,
    api: 'https://disqus.skk.moe/disqus/',
    apikey: 'dBfYxTiKx3J9iXzyiS000OB5a6Q2nk1dpCtXQZW3UnOKt0AoIvlqZ96bV2FsJiGp',
    admin: 'mingchiuli'
  })
  // 是要插入评论的地方
  disqus.render(document.getElementById('disqus')!)
  document.getElementById('utterance')?.appendChild(utterances)
})

</script>

<template>
  <div id="comment-div">
    <el-tabs :stretch=true v-model="activeName">
      <el-tab-pane label="Disqus" name="first">
        <div id="disqus" style="width: inherit"></div>
      </el-tab-pane>
      <el-tab-pane label="Utterance" name="second">
        <div id="utterance" style="width: inherit"></div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<style scoped>
#comment-div {
  margin: auto;
  width: 100%
}
</style>