<script lang="ts" setup>
import type { CatalogueLabel } from '@/type/entity'
import type { ElTree } from 'element-plus';
import { computed, nextTick, ref } from 'vue'
import Node from 'element-plus/es/components/tree/src/model/node'

const props = defineProps<{
  loadingCatalogue: boolean
}>()

const emit = defineEmits<(event: 'update:loadingCatalogue', payload: boolean) => void>()

let loadingCatalogue = computed({
  get() {
    return props.loadingCatalogue
  },
  set(value) {
    emit('update:loadingCatalogue', value)
  },
})

let data = ref<CatalogueLabel[]>()
let allNodes: Node[]
const defaultProps = { children: 'children', label: 'label' }
const rollGap = 10
const treeRef = ref<InstanceType<typeof ElTree>>()

const handleNodeClick = (data: CatalogueLabel) => window.scrollTo({ top: data.dist - rollGap, behavior: 'smooth' })

const render = async () => {
  let aLabels = document.querySelectorAll<HTMLElement>('.exhibit-content .v-note-wrapper a')
  const arrs = geneCatalogueArr(aLabels)
  if (arrs.length > 0) {
    data.value = arrs
    await nextTick()
    allNodes = treeRef.value!.store._getAllNodes()
  } else {
    loadingCatalogue.value = false
  }
}

const geneCatalogueArr = (aLabels: NodeListOf<HTMLElement>): CatalogueLabel[] => {
  const arr: CatalogueLabel[] = []
  for (let i = 0; i < aLabels.length; i++) {
    const aLabel = aLabels[i]
    const item: CatalogueLabel = {
      id: '',
      label: '',
      dist: 0,
      children: []
    }

    item.id = aLabel.id
    item.dist = aLabel.getBoundingClientRect().top
    item.label = aLabel.parentNode?.textContent as string
    item.children = getChildren(aLabels, i)
    i += getChildrenTotal(item.children)
    arr.push(item)
  }
  return arr
}

const getChildrenTotal = (children: CatalogueLabel[]): number => {
  if (children.length === 0) {
    return 0
  }

  let count = 0
  children.forEach(e => {
    count++
    count += getChildrenTotal(e.children)
  })
  return count
}

const getChildren = (aLabels: NodeListOf<HTMLElement>, index: number): CatalogueLabel[] => {
  const arr: CatalogueLabel[] = []
  if (index === aLabels.length - 1) {
    return arr
  }

  const curLabel = aLabels[index].parentNode!.nodeName
  //提取H{i}后面的i
  const curLabelNo = curLabel.substring(1)
  for (let i = index + 1; i < aLabels.length; i++) {
    const aLabel = aLabels[i]
    const labelNo = aLabel.parentNode!.nodeName.substring(1)

    if (parseInt(labelNo) > parseInt(curLabelNo)) {
      const item: CatalogueLabel = {
        id: '',
        label: '',
        dist: 0,
        children: []
      }

      item.id = aLabel.id
      item.dist = aLabel.getBoundingClientRect().top
      item.label = aLabel.parentNode?.textContent as string
      item.children = getChildren(aLabels, i)
      i += getChildrenTotal(item.children)
      arr.push(item)
    } else {
      break
    }
  }
  return arr
}

const rollToTargetLabel = (data: CatalogueLabel[], scrolled: number): CatalogueLabel => {
  let label: CatalogueLabel
  for (const element of data) {
    let dist = scrolled - element.dist
    if (dist > -rollGap) {
      label = element
      let childLabel = rollToTargetLabel(element.children, scrolled)
      if (childLabel) {
        label = childLabel
      }
    } else {
      break
    }
  }
  return label!
}

const roll = () => {
  if (allNodes) {
    let scrolled = document.documentElement.scrollTop
    let temp: CatalogueLabel
    temp = rollToTargetLabel(data.value as CatalogueLabel[], scrolled)!

    //高亮和关闭树节点的逻辑
    allNodes.forEach(node => {
      if (temp?.id === node.data.id) {
        node.expanded = true
        treeRef.value?.setCurrentKey(node.data.id)
      } else if (node.expanded) {
        node.expanded = false
      }
    })
    //处理顶级节点高亮不符合逻辑的问题
    if (!temp) {
      treeRef.value?.setCurrentKey(undefined)
    }
  }
}

const debounce = (fn: Function, interval = 100) => {
  let timeout: NodeJS.Timeout
  return () => {
    clearTimeout(timeout)
    timeout = setTimeout(function (this: Function) {
      fn.apply(this, arguments)
    }, interval)
  }
}
window.addEventListener('scroll', debounce(roll))

defineExpose({
  render
})
</script>

<template>
  <el-card shadow="never" class="box">
    <el-tree :data="data" :props="defaultProps" accordion @node-click="handleNodeClick" ref="treeRef" node-key="id"
      highlight-current />
  </el-card>
</template>

<style scoped>
.box {
  padding: 0px;
  min-height: 100px;
  position: fixed;
  width: 200px;
  margin-left: 70rem;
  margin-top: 30px;
  overflow-y: auto;
  overflow-x: auto;
}

.el-card:deep(.el-card__body) {
  padding: 5px;
}
</style>