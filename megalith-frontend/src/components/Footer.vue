<script lang="ts" setup> 
import { type AxiosResponse } from 'axios'
import axios from '../axios'
import type { Data } from '../type/entity'
import { ref, type Ref } from 'vue'

let start: Ref<number> = ref(2021)
let end: Ref<number> = ref(0)

axios.get('/public/blog/years')
  .then((resp: AxiosResponse<Data<number[]>>) => {
    const years : number[] = resp.data.data
    start.value = years[0]
    end.value = years[years.length - 1]
  })
</script>

<template>
  <div class="footer">
    <el-divider />
    <el-link :underline="false" class="copyright" href="/">&copy; Powered by Kubernetes {{ start }}-{{ end }} </el-link>
  </div>  
</template>

<style scoped>
.footer {
  max-width: 15rem;
  margin: 0 auto;
  text-align: center;
  margin-bottom: 1rem;
}
</style>