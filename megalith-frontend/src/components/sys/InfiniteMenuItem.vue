<script lang="ts" setup>
import { tabStore } from '@/stores'
import type { Menu, MenuNode, Tab } from '@/type/entity'
import router from '@/router'
import { RoutesEnum } from '@/type/entity'
import * as ElementPlusIcons from '@element-plus/icons-vue'

const props = defineProps<{
  item: Menu
}>()

const isRouteMenuNode = (node: MenuNode): node is Menu => node.type !== RoutesEnum.BUTTON
const visibleChildren = computed(() => props.item.children.filter(isRouteMenuNode))
const resolveMenuIcon = (name: string) =>
  ElementPlusIcons[name as keyof typeof ElementPlusIcons] ?? ElementPlusIcons.Menu

const selectMenu = (item: Tab) => {
  tabStore().addTab(item)
  router.push({ name: item.name })
}
</script>

<template>
  <!-- 没有子节点，使用 el-menu-item 渲染 -->
  <el-menu-item v-if="item.type === RoutesEnum.MENU" :index="item.name" @click="selectMenu(item)">
    <template #title>
      <el-icon :size="20">
        <component :is="resolveMenuIcon(item.icon)" />
      </el-icon>
      <span>{{ item.title }}</span>
    </template>
  </el-menu-item>

  <!-- 有子节点，使用 el-sub-menu 渲染 -->
  <el-sub-menu v-else-if="item.type === RoutesEnum.CATALOGUE" :index="String(item.id)">
    <template #title>
      <el-icon :size="20">
        <component :is="resolveMenuIcon(item.icon)" />
      </el-icon>
      <span>{{ item.title }}</span>
    </template>
    <!-- 递归渲染 -->
    <InfiniteMenuItem v-for="sub in visibleChildren" v-bind:key="sub.id" :item="sub" />
  </el-sub-menu>
</template>
