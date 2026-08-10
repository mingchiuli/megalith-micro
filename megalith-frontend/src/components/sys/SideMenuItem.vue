<script lang="ts" setup>
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import type { MenuInstance } from 'element-plus'
import { menuStore, tabStore } from '@/stores'
import { RoutesEnum, type Menu, type MenuNode } from '@/type/entity'

const { menuTree } = storeToRefs(menuStore())
const menuRef = ref<MenuInstance>()
const collapsed = ref(true)
const openCollapsedCatalogues = new Set<string>()
const arrow = computed(() => (collapsed.value ? ArrowRight : ArrowLeft))
const isRouteMenuNode = (node: MenuNode): node is Menu => node.type !== RoutesEnum.BUTTON
const visibleMenuItems = computed(() => menuTree.value?.children.filter(isRouteMenuNode) ?? [])
const reverseCollapse = (): void => {
  collapsed.value = !collapsed.value
}

const toggleCollapsedCatalogue = (index: string): void => {
  if (!collapsed.value) return
  if (openCollapsedCatalogues.has(index)) {
    menuRef.value?.close(index)
  } else {
    menuRef.value?.open(index)
  }
}

const trackOpenCatalogue = (index: string): void => {
  if (collapsed.value) openCollapsedCatalogues.add(index)
}

const trackClosedCatalogue = (index: string): void => {
  openCollapsedCatalogues.delete(index)
}

const closeCataloguePopups = (): void => {
  for (const index of openCollapsedCatalogues) menuRef.value?.close(index)
  openCollapsedCatalogues.clear()
}

onMounted(() => {
  collapsed.value = document.body.clientWidth <= 900
})

watch(collapsed, closeCataloguePopups)
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
    ref="menuRef"
    :default-active="tabStore().editableTabsValue"
    class="el-menu-vertical"
    :collapse="collapsed"
    :close-on-click-outside="collapsed"
    active-text-color="#ffd04b"
    @open="trackOpenCatalogue"
    @close="trackClosedCatalogue"
    @select="closeCataloguePopups"
  >
    <InfiniteMenuItem
      v-for="item in visibleMenuItems"
      v-bind:key="item.id"
      :item="item"
      :collapsed="collapsed"
      @open-catalogue="toggleCollapsedCatalogue"
    />
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
