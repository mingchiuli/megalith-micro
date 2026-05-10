<script setup lang="ts">
import { RouterView } from 'vue-router'
import { pageStore } from '@/stores'
</script>

<template>
  <router-view />
  <MyFooterItem v-if="pageStore().front" />
</template>
