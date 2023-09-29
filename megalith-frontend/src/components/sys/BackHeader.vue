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
    <el-text class="header-title" size="large">管理后台</el-text>
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
</template>

<style scoped>
.header {
  position: relative;
}

.header-title {
  position: absolute;
  left: 50%;
  transform: translate(-50%);
  top: 15px;
  line-height: 30px;
}

.header-avatar {
  position: absolute;
  top: 10px;
  right: 10px;
}

.header-dropdown {
  position: absolute;
  top: 20px;
  right: 70px;
  line-height: 20px;

}
</style>