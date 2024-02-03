<script setup lang="ts">
import { RouterView } from 'vue-router'
import { pageStore } from '@/stores/store'

</script>

<template>
  <router-view />
  <my-footer-item v-if="pageStore().front"/>
</template>