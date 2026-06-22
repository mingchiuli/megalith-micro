<script setup lang="ts">
import { pageStore } from '@/stores'
</script>

<template>
  <router-view />
  <MyFooterItem v-if="pageStore().front" />
</template>
