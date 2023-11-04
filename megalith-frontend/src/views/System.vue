<script lang="ts" setup>

</script>

<template>
  <el-container>
    <el-aside width="collapse">
      <side-menu />
    </el-aside>
    <el-container>
      <el-header height="100px">
        <back-header />
        <header-tabs />
      </el-header>
      <el-main>
        <div class="content">
          <router-view />
        </div>
      </el-main>
      <el-footer height="50px">
        <my-footer />
      </el-footer>
    </el-container>
  </el-container>
</template>

<style scoped>

.content {
  padding: 0 10px;
}

.el-header {
  padding: 0;
  margin-bottom: 20px;
}

.el-main {
  padding: 0;
  height: 550px;
}

</style>