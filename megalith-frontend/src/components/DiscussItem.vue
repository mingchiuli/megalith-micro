<script lang="ts" setup>
import { onMounted, ref } from 'vue'

const activeName = ref('first')

onMounted(() => {
  const giscus = document.createElement('script')
  giscus.type = 'text/javascript'
  giscus.async = true
  giscus.setAttribute('data-repo', 'mingchiuli/megalith-talk-repo')
  giscus.setAttribute('data-repo-id', 'R_kgDOGoA_3A')
  giscus.setAttribute('data-category', 'Announcements')
  giscus.setAttribute('data-category-id', 'DIC_kwDOGoA_3M4CiCk5')
  giscus.setAttribute('data-mapping', 'pathname')
  giscus.setAttribute('data-strict', '0')
  giscus.setAttribute('data-reactions-enabled', '1')
  giscus.setAttribute('data-emit-metadata', '0')
  giscus.setAttribute('data-input-position', 'bottom')
  giscus.setAttribute('data-theme', 'light')
  giscus.setAttribute('data-lang', 'zh-CN')
  giscus.crossOrigin = 'anonymous'
  giscus.src = 'https://giscus.app/client.js'

  const disqus = document.createElement('script')
  disqus.src = 'https://chiu-1.disqus.com/embed.js'
  disqus.setAttribute('data-timestamp', new Date().toString())

  document.getElementById('disqus_thread')!.appendChild(disqus)
  document.getElementById('utterance')!.appendChild(giscus)
})

</script>

<template>
  <div id="comment-div">
    <el-tabs :stretch=true v-model="activeName">
      <el-tab-pane label="Disqus" name="first">
        <div id="disqus_thread" style="width: inherit"></div>
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