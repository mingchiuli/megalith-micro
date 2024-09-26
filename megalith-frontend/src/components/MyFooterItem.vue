<script lang="ts" setup>
import { ref } from 'vue'
import { GET } from '@/http/http'

let start = ref(2021)
let end = ref<number>()

;(async () => {
  const years = await GET<number[]>('/public/blog/years')
  start.value = years[0]
  end.value = years[years.length - 1]
})()
</script>

<template>
  <div class="footer">
    <el-divider />
    <el-link :underline="false" class="copyright" href="/"
      >&copy; Powered by Docker {{ start }}-{{ end }}
    </el-link>
  </div>
</template>

<style scoped>
.footer {
  max-width: 15rem;
  margin: 0 auto;
  text-align: center;
}

.el-link {
  margin-bottom: 20px;
}
</style>
