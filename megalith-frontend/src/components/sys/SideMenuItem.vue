<script lang="ts" setup>
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { menuStore, tabStore } from '@/stores'
import { RoutesEnum, type Menu, type MenuNode } from '@/type/entity'

const { menuTree } = storeToRefs(menuStore())
const collapsed = ref(true)
const arrow = computed(() => (collapsed.value ? ArrowRight : ArrowLeft))
const isRouteMenuNode = (node: MenuNode): node is Menu => node.type !== RoutesEnum.BUTTON
const visibleMenuItems = computed(() => menuTree.value?.children.filter(isRouteMenuNode) ?? [])
const reverseCollapse = (): void => {
  collapsed.value = !collapsed.value
}

onMounted(() => {
  collapsed.value = document.body.clientWidth <= 900
})
</script>

<template>
  <el-button
    class="collapse-button"
    circle
    :icon="arrow"
    :aria-expanded="!collapsed"
    @click="reverseCollapse"
  ></el-button>
  <el-menu
    :default-active="tabStore().editableTabsValue"
    class="el-menu-vertical"
    :collapse="collapsed"
    active-text-color="#ffd04b"
  >
    <InfiniteMenuItem v-for="item in visibleMenuItems" v-bind:key="item.id" :item="item" />
  </el-menu>
</template>

<style scoped>
.el-menu-vertical:not(.el-menu--collapse) {
  width: 200px;
  min-height: 400px;
  border-right: none;
}

.el-menu--collapse {
  width: 65px;
  min-height: 400px;
  border-right: none;
}

.collapse-button {
  transform: translate(50%);
  margin-top: 15px;
  margin-bottom: 15px;
}
</style>
