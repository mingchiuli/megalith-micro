<script lang="ts" setup>

</script>

<template>
  <el-container>
    <el-aside width="collapse">
      <SideMenu></SideMenu>
    </el-aside>
    <el-container>
      <el-header height="60px">
        <BackHeader></BackHeader>
      </el-header>
      <el-main>
        <HeaderTabs></HeaderTabs>
        <div style="margin: 0 1%;">
          <router-view />
        </div>
      </el-main>
      <el-footer>
        <Footer />
      </el-footer>
    </el-container>
  </el-container>
</template>

<style scoped>
.el-header {
  background-color: #f6f6f6;
  padding: 0;
}

.el-main {
  padding: 0;
  height: 500px;
}
</style>