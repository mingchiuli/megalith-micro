<script lang="ts" setup>
import type { UserInfo } from '@/type/entity'


const info = localStorage.getItem('userinfo')
const user: UserInfo = JSON.parse(info!)

const avatar = user.avatar

</script>

<template>
  <div class="header">
    <el-text class="header-text" size="large">后台</el-text>
    <el-avatar class="header-avatar" size="medium" :src="avatar"></el-avatar>
  </div>
    
</template>

<style scoped>

.header {
  position: relative;
}

.header-text {
  position: absolute;
  left: 50%;
  top: 15px;
  line-height: 30px;
  height: 30px;
}

.header-avatar {
  position: absolute;
  top: 10px;
  right: 5%;
}
</style>