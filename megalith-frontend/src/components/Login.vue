<script lang="ts" setup>
import { ref } from 'vue';

const centerDialogVisible = ref(true)
</script>

<template>
  <el-dialog v-model="centerDialogVisible" center close-on-press-escape fullscreen align-center>
    <template #default>
    </template>
    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary">登录</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>

</style>