<script lang="ts" setup>
import type { UserInfo } from '@/type/entity'
import { ArrowDown } from '@element-plus/icons-vue'
import { clearLoginState } from '@/utils/tools'
import router from '@/router'
import { storage } from '@/utils/storage'

const user = storage.getUserInfo<UserInfo>()

const avatar = ref(user?.avatar || '')
const nickname = ref(user?.nickname || '')

const goToHome = () => {
  router.push('/blogs')
}

const logout = () => {
  clearLoginState()
  goToHome()
}
</script>

<template>
  <div class="header-container">
    <el-text class="header-title" size="large"
      >{{ $t('admin.backend') }}
      <el-dropdown class="header-dropdown">
        <span class="el-dropdown-link">
          {{ nickname }}
          <el-icon class="el-icon--right">
            <arrow-down />
          </el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="goToHome">{{ $t('admin.home') }}</el-dropdown-item>
            <el-dropdown-item divided @click="logout">{{ $t('admin.logout') }}</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
      <el-avatar class="header-avatar" size="default" :src="avatar"></el-avatar>
    </el-text>
  </div>
</template>
<style scoped>
.header-container {
  width: 100%;
  padding-bottom: 8px;
}

.header-title {
  text-align: center;
  line-height: 60px;
  display: inline-block;
  width: 100%;
  border-bottom: 1px solid var(--el-border-color);
}

/* 用line-height对于图片时失效 */
.header-avatar {
  margin-top: 10px;
  height: 40px;
  position: absolute;
  right: 10px;
}

.header-dropdown {
  line-height: 60px;
  position: absolute;
  right: 60px;
}
</style>
