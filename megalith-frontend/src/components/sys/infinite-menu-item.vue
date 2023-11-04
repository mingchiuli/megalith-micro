<script lang="ts" setup>
import { tabStore } from '@/stores/store';
import type { Menu, Tab } from '@/type/entity';
import router from '@/router'

defineProps<{
  item: Menu
}>()

const selectMenu = (item: Tab) => {
  tabStore().addTab(item)
  router.push({ name: item.name })
}

</script>

<template>
  <!-- 没有子节点，使用 el-menu-item 渲染 -->
  <el-menu-item v-if="!item.children.length" :index="item.name" @click="selectMenu(item)">
    <template #title>
      <el-icon :size=20>
        <component :is="item.icon" />
      </el-icon>
      <span>{{ item.title }}</span>
    </template>
  </el-menu-item>


  <!-- 有子节点，使用 el-sub-menu 渲染 -->
  <el-sub-menu v-else :index="String(item.menuId)">
    <template #title>
      <el-icon :size=20>
        <component :is="item.icon" />
      </el-icon>
      <span>{{ item.title }}</span>
    </template>
    <!-- 循环渲染 -->
    <infinite-menu-item v-for="sub in item.children" :item="sub" />
  </el-sub-menu>
</template>

<style scoped></style>