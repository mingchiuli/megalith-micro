<script lang="ts" setup>
import { reactive, ref, type Ref } from 'vue';
import type { Hot, Data } from '../type/entity'
import { type AxiosResponse } from 'axios'
import axios from '../axios';

let hots: Ref<Hot[]> = ref([])

axios.get('/public/blog/scores')
  .then((resp: AxiosResponse<Data<Hot[]>>) => {
    hots.value = resp.data.data
  })

</script>

<template>
<el-card shadow="never" class="hot-blogs">
  <div class="title">本周热读</div>
  <div class="description" v-for="hot in hots">{{ hot.title }}: {{ hot.readCount }}次</div>
</el-card> 
</template>

<style scoped>
.hot-blogs {
  max-width: 250px;
  margin-bottom: 20px
}

.title {
  text-align: center;
  margin-bottom: 20px
}

.description {
  text-align: left
}
</style>