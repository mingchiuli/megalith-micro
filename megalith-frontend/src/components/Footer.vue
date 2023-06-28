<script lang="ts" setup> 
import { type AxiosResponse } from 'axios';
import axios from '../axios';
import type { Data } from "../type/entity";
import { ref, type Ref } from 'vue';

let start : Ref<number> = ref(0)
let end : Ref<number> = ref(0)

axios.get<Data<number[]>>('/public/blog/years').then((resp: AxiosResponse) => {
  const years : number[] = resp.data.data
  start.value = years[0]
  end.value = years[1]
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
  max-width: 250px;
  margin: 0 auto;
  text-align: center;
  margin-bottom: 30px;
}
</style>