<script lang="ts" setup>
import type { CatalogueLabel } from '@/type/entity'

const render = () => {
  let aLabel = document.querySelectorAll<HTMLElement>('.exhibit-content .v-note-wrapper a')
  const arr = geneDist(aLabel)



  aLabel.forEach(e => {
    console.log(e)
    console.log(e.parentNode)
  })
}

const geneDist = (aLabel: NodeListOf<HTMLElement>): CatalogueLabel[] => {
  const arr: CatalogueLabel[] = []
  aLabel.forEach(label => {
    arr.push({ 'id': label.id, 'dist': label.offsetTop })
  })
  return arr
}


defineExpose({
  render
});
</script>

<template>
  <el-card shadow="never" class="box">
    aaa
  </el-card>
</template>

<style scoped>
.box {
  position: fixed;
  width: 200px;
  margin-left: 70rem;
  margin-top: 30px;
}
</style>