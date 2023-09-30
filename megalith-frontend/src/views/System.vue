<script lang="ts" setup>

</script>

<template>
  <div class="common-layout">
    <el-container>
      <el-aside width="collapse">
        <SideMenu></SideMenu>
      </el-aside>
      <el-container>
        <el-header class="header">
          <BackHeader></BackHeader>
        </el-header>
        <el-main>
          <HeaderTabs></HeaderTabs>
          <div style="margin: 0 1%;">
            <router-view />
          </div>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<style scoped>
.header {
  padding: 0;
}

.el-header {
  background-color: #f6f6f6;
  color: #333;
}

.el-main {
  padding: 0;
}
</style>