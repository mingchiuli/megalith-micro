<script lang="ts" setup>
import type { BlogsDesc, Data, PageAdapter } from '@/type/entity';
import axios from '../axios';
import { ref, type Ref } from 'vue'
import type { AxiosResponse } from 'axios';

const emit = defineEmits<(event: "search", payload: PageAdapter<BlogsDesc>) => void>();

const state = ref('')

const outerVisible: Ref<boolean> = ref(false)
const innerVisible: Ref<boolean> = ref(false)

let timeout: NodeJS.Timeout
const querySearchAsync = async (queryString: string, cb: any) => {
  const resp : AxiosResponse<Data<PageAdapter<BlogsDesc>>> = await axios.get(`/search/blog/1?keywords=${queryString}&allInfo=false`)
  emit("search" ,resp.data.data)
  resp.data.data.content.forEach((blogsDesc: BlogsDesc) => {
    blogsDesc.value = blogsDesc.highlight
  })
  //节流
  clearTimeout(timeout)
  timeout = setTimeout(() => {
    cb(resp.data.data.content)
  }, 1000 * Math.random())
}
  
const handleSelect = (item: BlogsDesc) => console.log(item)

// querySearchAsync("测试", 1, false)

</script>
<template>
  <el-button class="search-button" @click="outerVisible = true" type="success">Search</el-button>
  <el-dialog v-model="outerVisible" fullscreen center align-center>
    <template #default>
      <div class="dialog-content">
        <el-autocomplete v-model="state" 
        :fetch-suggestions="querySearchAsync" 
        placeholder="Please input" 
        @select="handleSelect"
        :trigger-on-focus="false">
        <template #default="{ item }">          
          <div class="value" v-if="item.value.title" v-html="item.value.title"></div>
          <div class="value" v-if="item.value.description" v-html="item.value.description"></div>
          <div class="value" v-if="item.value.content" v-html="item.value.content"></div>
        </template>
      </el-autocomplete>
      </div>
      <el-dialog v-model="innerVisible" width="50%" append-to-body title="Archieve">aaa</el-dialog>
    </template>
    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" @click="outerVisible = false">Confirm</el-button>
        <el-button type="primary" @click="innerVisible = true">Archieve</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.dialog-content {
  margin: 0 auto;
  max-width: max-content;
}

.search-button {
  position: absolute;
  right: 0;
  z-index: 1;
}

</style>