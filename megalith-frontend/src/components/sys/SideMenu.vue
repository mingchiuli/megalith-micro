<script lang="ts" setup>
import { ref } from 'vue'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import router from '@/router'
import { menuStore, tabStore } from '@/stores/store'
import type { Tab } from '@/type/entity'
import { storeToRefs } from 'pinia'

const isCollapse = ref(false)
const { menuList } = storeToRefs(menuStore())
const reverseCollapse = () => isCollapse.value = !isCollapse.value
const to = (name: string) => router.push({ name: name })

const selectMenu = (item: Tab) => {
  tabStore().addTab(item)
  router.push({ name: item.name })
}
</script>

<template>
  <el-button class="collapse-button" circle :icon="ArrowLeft" v-if="!isCollapse" @click="reverseCollapse"></el-button>
  <el-button class="collapse-button" circle :icon="ArrowRight" v-if="isCollapse" @click="reverseCollapse"></el-button>
  <el-menu :default-active="tabStore().editableTabsValue" class="el-menu-vertical" :collapse="isCollapse"
    active-text-color="#ffd04b">
    <el-sub-menu :index="String(menu.menuId)" v-for="menu in menuList">
      <template #title>
        <el-icon :size=20>
          <component :is="menu.icon" />
        </el-icon>
        <span>{{ menu.title }}</span>
      </template>

      <a @click="to(item.name)" v-for="item in menu.children">
        <el-menu-item :index="item.name" @click="selectMenu(item)">
          <template #title>
            <el-icon :size=20>
              <component :is="item.icon" />
            </el-icon>
            <span>{{ item.title }}</span>
          </template>
        </el-menu-item>
      </a>
    </el-sub-menu>
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