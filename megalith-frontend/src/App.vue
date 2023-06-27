<script setup lang="ts">
import { RouterView } from 'vue-router'
</script>

<template>
  <RouterView />
  <Suspense>
    <Footer />
  </Suspense>
</template>