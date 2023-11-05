<script lang="ts" setup>
import { ref } from 'vue'
const value = ref(new Date())
</script>

<template>
  <el-card class="hint" shadow="never"> 404 NOT FOUND </el-card>
  <el-calendar class="calendar" v-model="value" />
  <my-footer />
</template>

<style scoped>
.hint {
  text-align: center;
}

.calendar {
  margin: 0 auto;
  max-width: 600px;
}
</style>