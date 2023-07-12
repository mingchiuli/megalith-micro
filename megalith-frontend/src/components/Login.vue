<script lang="ts" setup>
import { ref } from 'vue';
const input = ref('')

const centerDialogVisible = ref(true)
</script>

<template>
  <el-dialog v-model="centerDialogVisible" center close-on-press-escape fullscreen align-center>
    <template #default>
      <div>
        <el-input v-model="input" placeholder="Please input username" />
      </div>
      <div>
        <el-input v-model="input" type="password" placeholder="Please input password" show-password />
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
  top: 50%;
  transform: translate(-50%, -50%);
}
</style>