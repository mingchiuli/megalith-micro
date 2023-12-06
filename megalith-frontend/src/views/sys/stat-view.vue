<script lang="ts" setup>
import { onMounted } from 'vue'

onMounted(() => {
  const clustrmaps = document.createElement('script')
  clustrmaps.type = 'text/javascript'
  clustrmaps.id = 'clustrmaps'
  clustrmaps.src = '//cdn.clustrmaps.com/map_v2.js?cl=ffffff&w=800&t=n&d=axViRjet6dzNbyFy3Ga1gq-OT4fMp4qg_lIdwsiLNXo'
  document.getElementById('map')!.appendChild(clustrmaps)
})
</script>

<template>
  <div id="map"></div>
</template>

<style scoped>
#map {
  margin-top: 30px
}
</style>