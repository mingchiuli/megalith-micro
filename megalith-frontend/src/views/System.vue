<script lang="ts" setup>
</script>

<template>
  <el-container style="height: 100vh">
    <el-aside width="200">
      <el-scrollbar>
        <side-menu />
      </el-scrollbar>
    </el-aside>
    <el-container>
      <el-header height="100">
        <back-header />
        <header-tabs />
      </el-header>
      <el-main>
        <div class="content">
          <router-view />
        </div>
      </el-main>
      <el-footer>
        <my-footer />
      </el-footer>
    </el-container>
  </el-container>
</template>

<style scoped>
.el-header {
  --el-header-padding: 0
}

.el-footer {
  --el-footer-height: 100
}
</style>