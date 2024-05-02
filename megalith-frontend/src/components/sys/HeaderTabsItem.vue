<script lang="ts" setup>
import router from '@/router'
import { displayStateStore, tabStore } from '@/stores/store'
import type { Tab } from '@/type/entity'
import type { TabPaneName, TabsPaneContext } from 'element-plus'
import { storeToRefs } from 'pinia'

const { editableTabs, editableTabsValue } = storeToRefs(tabStore())

const clickTab = (tab: TabsPaneContext) => router.push({ name: String(tab.props.name) })
const removeTab = (name: TabPaneName): any => {
  let changed = false
  const tabs: Tab[] = editableTabs.value
  if (tabs.length === 1) {
    editableTabs.value = tabs.filter(tab => tab.name !== name)
    displayStateStore().welcomeBackend = true
    router.push({ name: 'system' })
    return
  }

  if (editableTabsValue.value === name) {
    tabs.forEach((tab, idx) => {
      if (tab.name === name) {
        let nextTab = tabs[idx + 1] || tabs[idx - 1]
        if (nextTab) {
          tabStore().editableTabsValue = nextTab.name
          changed = true
        }
      }
    })
  }
  editableTabs.value = tabs.filter(tab => tab.name !== name)
  if (changed) router.push({ name: editableTabsValue.value })
}
</script>

<template>
  <el-tabs v-model="editableTabsValue" type="card" closable @tab-remove="removeTab" @tab-click="clickTab">
    <el-tab-pane v-for="item in editableTabs" :key="item.name" :label="item.title" :name="item.name">
    </el-tab-pane>
  </el-tabs>
</template>