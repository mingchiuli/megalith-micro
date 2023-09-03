<script lang="ts" setup>
import type { CatalogueLabel } from '@/type/entity'

const render = () => {
  let aLabels = document.querySelectorAll<HTMLElement>('.exhibit-content .v-note-wrapper a')
  const arr = geneCatalogueArr(aLabels)
  console.log(arr)
}

const geneCatalogueArr = (aLabels: NodeListOf<HTMLElement>): CatalogueLabel[] => {
  const arr: CatalogueLabel[] = []
  for (let i = 0; i < aLabels.length; i++) {
    const aLabel = aLabels[i]
    const item: CatalogueLabel = {
      id: '',
      name: '',
      href: '',
      dist: 0,
      children: undefined
    }

    item.id = aLabel.id
    item.dist = aLabel.offsetTop
    item.href = '#' + aLabel.id
    item.name = aLabel.parentNode?.textContent
    item.children = getChildren(aLabels, i)
    i += getChildrenTotal(item.children)
    arr.push(item)
  }
  return arr
}

const getChildrenTotal = (children: CatalogueLabel[] | undefined): number => {
  if (!children) {
    return 0
  }

  let count = 0;
  children.forEach(e => {
    count++;
    count += getChildrenTotal(e.children)
  })
  return count
}

const getChildren = (aLabels: NodeListOf<HTMLElement>, index: number): CatalogueLabel[] => {
  const arr: CatalogueLabel[] = []
  if (index === aLabels.length - 1) {
    return arr
  }

  const curLabel = aLabels[index].parentNode?.nodeName as string
  const curLabelNo = curLabel.substring(1) as string
  for (let i = index + 1; i < aLabels.length; i++) {
    const aLabel = aLabels[i]
    const labelNo = aLabel.parentNode?.nodeName.substring(1) as string

    if (parseInt(labelNo) > parseInt(curLabelNo)) {
      const item: CatalogueLabel = {
        id: '',
        name: '',
        href: '',
        dist: 0,
        children: undefined
      }

      item.id = aLabel.id
      item.dist = aLabel.offsetTop
      item.href = '#' + aLabel.id
      item.name = aLabel.parentNode?.textContent
      item.children = getChildren(aLabels, i)
      i += getChildrenTotal(item.children)
      arr.push(item)
    } else {
      break
    }
  }
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