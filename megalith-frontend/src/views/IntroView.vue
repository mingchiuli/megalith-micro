<script setup lang="ts">
import avatar from '@/assets/logo.svg'
import { Notebook } from '@element-plus/icons-vue'

const toGithub = () => window.location.href = 'https://github.com/mingchiuli'
</script>

<template>
  <div class="front">
    <div id="into-button-father">
      <div class="into-button">
        <el-button id="into-button-github" circle @click="toGithub">
          <i class="el-icon" style="font-size: 20px;">
            <svg viewBox="0 0 24 24">
              <path
                d="M12 2C6.475 2 2 6.475 2 12a9.994 9.994 0 0 0 6.838 9.488c.5.087.687-.213.687-.476c0-.237-.013-1.024-.013-1.862c-2.512.463-3.162-.612-3.362-1.175c-.113-.288-.6-1.175-1.025-1.413c-.35-.187-.85-.65-.013-.662c.788-.013 1.35.725 1.538 1.025c.9 1.512 2.338 1.087 2.912.825c.088-.65.35-1.087.638-1.337c-2.225-.25-4.55-1.113-4.55-4.938c0-1.088.387-1.987 1.025-2.688c-.1-.25-.45-1.275.1-2.65c0 0 .837-.262 2.75 1.026a9.28 9.28 0 0 1 2.5-.338c.85 0 1.7.112 2.5.337c1.912-1.3 2.75-1.024 2.75-1.024c.55 1.375.2 2.4.1 2.65c.637.7 1.025 1.587 1.025 2.687c0 3.838-2.337 4.688-4.562 4.938c.362.312.675.912.675 1.85c0 1.337-.013 2.412-.013 2.75c0 .262.188.574.688.474A10.016 10.016 0 0 0 22 12c0-5.525-4.475-10-10-10z">
              </path>
            </svg>
          </i>
        </el-button>
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
        2023-now, Software Engineering, Software Backend Programmer<br />
        2020-2023, Jinan University, International Trade, Master of Economics degree<br />
        2014-2018, Northwest A&F University, Grape and Wine Engineering, Bachelor of Engineering degree<br />
        <br />
        The technology stack used in the front-end of this website are Vue.js and Element Plus(A UI framework). I tried
        to write the front-end code with TypeScript.<br />
        The technology stack used in the back-end of this website are Java and Spring.<br />
        The program is deployed in Kubernetes.<br />
        All codes for this program can be obtained in GitHub. If you want to get them, please click the left
        button.<br />
      </el-text>
    </el-card>
    <StatItem />
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
