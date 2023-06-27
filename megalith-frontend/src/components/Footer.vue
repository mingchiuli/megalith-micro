<script lang="ts" setup> 
import { inject } from 'vue';
const axios: any = inject('$axios')
const data = await axios.get('/public/blog/years')
let start : number = data[0]
let end : number = data[1]
</script>

<template>
  <div class="footer">
    <el-divider />
    <el-link :underline="false" class="copyright" href="/">&copy; Powered by Kubernetes {{start}}-{{end}} </el-link>
  </div>  
</template>

<style scoped>
.footer {
  max-width: 250px;
  margin: 0 auto;
  text-align: center;
  margin-bottom: 25px;
}
</style>