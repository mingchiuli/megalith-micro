<script lang="ts" setup>
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

  const disqus = document.createElement('script')
  disqus.src = 'https://chiu-1.disqus.com/embed.js'
  disqus.setAttribute('data-timestamp', new Date().toString())

  document.getElementById('disqus_thread')!.appendChild(disqus)
  document.getElementById('utterance')!.appendChild(utterances)
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