<script setup lang="ts">
import avatar from '@/assets/logo.svg'
import { Notebook, Download } from '@element-plus/icons-vue'

const toGithub = () => window.location.href = 'https://github.com/mingchiuli'
</script>

<template>
  <div id="into-button-father">
    <div class="into-button">
      <el-button id="into-button-github" circle :icon="Download" @click="toGithub"></el-button>
      <RouterLink to='/blogs'>
        <el-button circle :icon="Notebook"></el-button>
      </RouterLink>
    </div>
  </div>
  <el-avatar shape="square" :size="200" :src=avatar fit="cover" />
  <el-card class="box-card" shadow="never">
    <div v-for="o in 15" :key="o" class="text item">{{ 'List item ' + o }}</div>
  </el-card>
  <Stat></Stat>
</template>

<style>
@import '@/assets/front.css';

#into-button-father {
  position: relative;
}

#into-button-github {
  margin-right: 5px;
}

.into-button {
  margin-top: 20px;
  position: absolute;
  right: 0;
}
</style>
