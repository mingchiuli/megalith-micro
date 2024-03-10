<script lang="ts" setup>
import type { UserInfo } from '@/type/entity'
import { ArrowDown } from '@element-plus/icons-vue'
import { clearLoginState } from '@/utils/tools'
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
  <el-text class="header-title" size="large">后台
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
  </el-text>
</template>
<style scoped>

.header-title {
  text-align: center;
  line-height: 60px;
  background-color: #f6f6f6;
  display: inline-block;
  width: 100%
}

/* 用line-height对于图片时失效 */
.header-avatar {
  margin-top: 10px;
  height: 40px;
  position: absolute;
  right: 10px
}

.header-dropdown {
  line-height: 60px;
  position: absolute;
  right: 60px
}
</style>