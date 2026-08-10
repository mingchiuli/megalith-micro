<script lang="ts" setup>
import { menuStore, pageStore } from '@/stores'

const page = pageStore()
const route = useRoute()
const isSystemRoot = computed(() => route.name === menuStore().menuTree?.name)

page.front = false

onUnmounted(() => {
  page.front = true
})
</script>

<template>
  <el-container style="height: 100vh">
    <el-aside width="200">
      <el-scrollbar>
        <SideMenuItem />
      </el-scrollbar>
    </el-aside>
    <el-container>
      <el-header height="100">
        <BackHeaderItem />
        <HeaderTabsItem />
      </el-header>
      <el-main>
        <el-text v-if="isSystemRoot" class="welcome">
          {{ $t('admin.welcome') }}
        </el-text>
        <div v-else class="content">
          <router-view />
        </div>
      </el-main>
      <el-footer>
        <MyFooterItem />
      </el-footer>
    </el-container>
  </el-container>
</template>

<style scoped>
.el-header {
  --el-header-padding: 0;
}

.el-footer {
  --el-footer-height: 100;
}

.welcome {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  font-size: xx-large;
}
</style>
