<script lang="ts" setup>
import { ref } from 'vue'
import { ArrowLeft, ArrowRight, Document, Menu as IconMenu, Location, Setting} from '@element-plus/icons-vue'


const isCollapse = ref(true)
const reverseCollapse = () => isCollapse.value = !isCollapse.value

const handleOpen = (key: string, keyPath: string[]) => {
  console.log(key, keyPath)
}
const handleClose = (key: string, keyPath: string[]) => {
  console.log(key, keyPath)
}
</script>

<template>
  <el-button class="collapse-button" circle :icon="ArrowLeft" v-if="!isCollapse" @click="reverseCollapse"></el-button>
  <el-button class="collapse-button" circle :icon="ArrowRight" v-if="isCollapse" @click="reverseCollapse"></el-button>


  <el-menu default-active="2" class="el-menu-vertical-demo" :collapse="isCollapse" @open="handleOpen"
    @close="handleClose">
    <el-sub-menu index="1">
      <template #title>
        <el-icon>
          <location />
        </el-icon>
        <span>Navigator One</span>
      </template>
      <el-menu-item-group>
        <template #title><span>Group One</span></template>
        <el-menu-item index="1-1">item one</el-menu-item>
        <el-menu-item index="1-2">item two</el-menu-item>
      </el-menu-item-group>
      <el-menu-item-group title="Group Two">
        <el-menu-item index="1-3">item three</el-menu-item>
      </el-menu-item-group>
      <el-sub-menu index="1-4">
        <template #title><span>item four</span></template>
        <el-menu-item index="1-4-1">item one</el-menu-item>
      </el-sub-menu>
    </el-sub-menu>
    <el-menu-item index="2">
      <el-icon><icon-menu /></el-icon>
      <template #title>Navigator Two</template>
    </el-menu-item>
    <el-menu-item index="3" disabled>
      <el-icon>
        <document />
      </el-icon>
      <template #title>Navigator Three</template>
    </el-menu-item>
    <el-menu-item index="4">
      <el-icon>
        <setting />
      </el-icon>
      <template #title>Navigator Four</template>
    </el-menu-item>
  </el-menu>
</template>

<style scoped>
.el-menu-vertical-demo:not(.el-menu--collapse) {
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
}
</style>