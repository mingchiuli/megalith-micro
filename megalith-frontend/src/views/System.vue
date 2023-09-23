<script lang="ts" setup>

</script>

<template>
  <div class="common-layout">
    <el-container>
      <el-aside width="200px">Aside</el-aside>
      <el-container>
        <el-header>Header</el-header>
        <div style="margin: 0 1%;">
          <router-view />
        </div>
      </el-container>
    </el-container>
  </div>
</template>

<style scoped></style>