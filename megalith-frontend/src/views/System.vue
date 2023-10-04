<script lang="ts" setup>

</script>

<template>
  <el-container>
    <el-aside width="collapse">
      <SideMenu></SideMenu>
    </el-aside>
    <el-container>
      <el-header height="100px">
        <BackHeader></BackHeader>
        <HeaderTabs></HeaderTabs>
      </el-header>
      <el-main>
        <div class="content">
          <router-view />
        </div>
      </el-main>
      <el-footer height="50px">
        <Footer />
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