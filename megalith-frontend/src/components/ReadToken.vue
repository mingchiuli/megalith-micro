<script lang="ts" setup>
import { ref } from 'vue';


const dialogVisible = ref(true)

const input = ref('')

const handleClose = () => {

}
</script>

<template>
  <el-dialog v-model="dialogVisible" title="阅读码" width="300px" :before-close="handleClose">
    <el-input v-model="input" type="password" placeholder="Please input password" show-password />
    <template #footer>
      <span class="dialog-footer">
        <el-button type="primary" @click="dialogVisible = false">
          提交
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<style scoped></style>