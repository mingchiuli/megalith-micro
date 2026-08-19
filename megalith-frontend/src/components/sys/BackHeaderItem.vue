<script lang="ts" setup>
import { ArrowDown } from '@element-plus/icons-vue'
import { useAuth } from '@/utils/auth'
import { loginStateStore } from '@/stores'

const router = useRouter()
const { logout } = useAuth()
const { user } = storeToRefs(loginStateStore())

const avatar = computed(() => user.value?.avatar || '')
const nickname = computed(() => user.value?.nickname || '')

const goToHome = () => {
  router.push('/blogs')
}

const handleLogout = async () => {
  try {
    await logout()
  } finally {
    goToHome()
  }
}
</script>

<template>
  <div class="header-container">
    <div class="header-content">
      <el-text class="header-title" size="large">{{ $t('admin.backend') }}</el-text>
      <div class="header-actions">
        <el-dropdown class="header-dropdown" :trigger="['click', 'hover']">
          <span class="el-dropdown-link">
            {{ nickname }}
            <el-icon class="el-icon--right">
              <arrow-down />
            </el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="goToHome">{{ $t('admin.home') }}</el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">{{
                $t('admin.logout')
              }}</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-avatar class="header-avatar" size="default" :src="avatar"></el-avatar>
      </div>
    </div>
  </div>
</template>
<style scoped>
.header-container {
  width: 100%;
  padding-bottom: 8px;
}

.header-content {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto minmax(0, 1fr);
  align-items: center;
  text-align: center;
  width: 100%;
  min-height: 60px;
  border-bottom: 1px solid var(--el-border-color);
}

.header-title {
  grid-column: 2;
}

.header-actions {
  grid-column: 3;
  justify-self: end;
  display: flex;
  align-items: center;
  gap: 10px;
  margin-right: 10px;
}

.header-dropdown {
  line-height: normal;
}

.el-dropdown-link {
  display: inline-flex;
  align-items: center;
  cursor: pointer;
}
</style>
