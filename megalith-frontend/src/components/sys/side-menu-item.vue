<script lang="ts" setup>
import { shallowRef } from 'vue'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { menuStore, tabStore, displayStateStore } from '@/stores/store'
import { storeToRefs } from 'pinia'

const { expand } = storeToRefs(displayStateStore())
const { menuList } = storeToRefs(menuStore())
let arrow = shallowRef(expand.value ? ArrowLeft : ArrowRight)
const reverseCollapse = () => {
  expand.value = !expand.value
  if (!expand.value) {
    arrow.value = ArrowRight
  } else {
    arrow.value = ArrowLeft
  } 
}

</script>

<template>
  <el-button class="collapse-button" circle :icon="arrow" @click="reverseCollapse"></el-button>
  <el-menu :default-active="tabStore().editableTabsValue" class="el-menu-vertical" :collapse="!expand"
    active-text-color="#ffd04b">
    <infinite-menu-item v-for="item in menuList" v-bind:key="item.menuId" :item="item" />
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