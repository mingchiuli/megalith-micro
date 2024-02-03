<script setup lang="ts">
import avatar from '@/assets/logo.svg'
import { Notebook, Download } from '@element-plus/icons-vue'

const toGithub = () => window.location.href = 'https://github.com/mingchiuli'
</script>

<template>
  <div class="front">
    <div id="into-button-father">
      <div class="into-button">
        <el-button id="into-button-github" circle :icon="Download" @click="toGithub"></el-button>
        <router-link to='/blogs'>
          <el-button circle :icon="Notebook"></el-button>
        </router-link>
      </div>
    </div>
    <el-avatar shape="square" :size="200" :src=avatar fit="cover" />
    <el-card class="box-card" shadow="never">
      <el-text size="large">
        This is Adam Lea, a junior software developer who is majoring in Java.<br />
        His short CV is following:<br />
        <br />
        2023-now, Boss Zhipin(NASDAQ:BZ), Software Engineering, Software Backend Programmer<br />
        2020-2023, Jinan University, International Trade, Master of Economics degree<br />
        2014-2018, Northwest A&F University, Grape and Wine Engineering, Bachelor of Engineering degree<br />
        <br />
        The technology stack used in the front-end of this website are Vue.js and Element Plus(A UI framework). I tried to write the front-end code with TypeScript.<br />
        The technology stack used in the back-end of this website are Java and Spring.<br />
        The program is deployed in Kubernetes.<br />
        All codes for this program can be obtained in GitHub. If you want to get them, please click the left button.<br />
      </el-text>
    </el-card>
    <stat-item />
  </div>
</template>

<style>
@import '@/assets/front.css';

#into-button-father {
  position: relative
}

#into-button-github {
  margin-right: 5px
}

.into-button {
  margin-top: 20px;
  position: absolute;
  right: 0
}
</style>
