<script lang="ts" setup>
import { ref } from 'vue';
import { loginStateStore } from '@/stores/store'
import { storeToRefs } from 'pinia';
import router from '@/router'

const input = ref('')
const { login } = storeToRefs(loginStateStore())

const beforeClose = (close: Function) => {
  login.value = true
  router.push({
    name: 'blogs'
  })
  close()
}

</script>

<template>
  <el-dialog v-model="login" center close-on-press-escape fullscreen align-center :before-close="beforeClose">
    <template #default>
      <div class="input">
        <div>
          <el-input v-model="input" placeholder="username" clearable />
        </div>
        <div>
          <el-input v-model="input" type="password" placeholder="password" show-password clearable />
        </div>
      </div>
    </template>
    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary">登录</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.el-input {
  width: 200px;
  left: 50%;
  transform: translate(-50%);
  margin-top: 10px;
}

.dialog-footer {
  margin-top: 0;
}
</style>