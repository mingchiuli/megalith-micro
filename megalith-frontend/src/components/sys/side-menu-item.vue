<script lang="ts" setup>
import { ref } from 'vue'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { menuStore, tabStore } from '@/stores/store'
import { storeToRefs } from 'pinia'

const isCollapse = ref(false)
const { menuList } = storeToRefs(menuStore())
const reverseCollapse = () => isCollapse.value = !isCollapse.value

</script>

<template>
  <el-button class="collapse-button" circle :icon="ArrowLeft" v-if="!isCollapse" @click="reverseCollapse"></el-button>
  <el-button class="collapse-button" circle :icon="ArrowRight" v-if="isCollapse" @click="reverseCollapse"></el-button>
  <el-menu :default-active="tabStore().editableTabsValue" class="el-menu-vertical" :collapse="isCollapse"
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