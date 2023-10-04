<script lang="ts" setup>
import type { UserInfo } from '@/type/entity'
import { ArrowDown } from '@element-plus/icons-vue'
import { clearLoginState } from '@/utils/common'
import router from '@/router'

const info = localStorage.getItem('userinfo')
const user: UserInfo = JSON.parse(info!)
const avatar = user.avatar
const nickname = user.nickname

const logout = () => {
  clearLoginState()
  router.push('/blogs')
}

</script>

<template>
  <div class="header">
    <div class="header-center">
      <el-text class="header-title" size="large">后台</el-text>
    </div>
    <div class="header-right"> 
      <el-dropdown class="header-dropdown">
        <span class="el-dropdown-link">
          {{ nickname }}
          <el-icon class="el-icon--right">
            <arrow-down />
          </el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="router.push('/blogs')">回到首页</el-dropdown-item>
            <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
      <el-avatar class="header-avatar" size="default" :src="avatar"></el-avatar>
    </div>

  </div>
</template>

<style scoped>
.header {
  background-color: #f6f6f6;
  display: flex;
  height: 60px;
}

.header-center {
  display: flex;
  margin-right: 20%;
  margin-left: 50%;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
}
</style>