<script lang="ts" setup>
import { menuStore, tabStore } from '@/stores'
import type { TabPaneName, TabsPaneContext } from 'element-plus'

const router = useRouter()
const tabs = tabStore()
const { editableTabs, editableTabsValue } = storeToRefs(tabs)

const clickTab = (tab: TabsPaneContext) => router.push({ name: String(tab.props.name) })
const removeTab = async (name: TabPaneName) => {
  const tabName = String(name)
  const currentTabs = editableTabs.value

  if (currentTabs.length === 1 && currentTabs[0]?.name === tabName) {
    tabs.removeTab(tabName)
    tabs.editableTabsValue = ''
    const rootName = menuStore().menuTree?.name
    if (rootName) await router.push({ name: rootName })
    return
  }

  if (editableTabsValue.value !== tabName) {
    tabs.removeTab(tabName)
    return
  }

  const currentIndex = currentTabs.findIndex((tab) => tab.name === tabName)
  const nextTab = currentTabs[currentIndex + 1] ?? currentTabs[currentIndex - 1]
  tabs.removeTab(tabName)
  if (!nextTab) return

  tabs.editableTabsValue = nextTab.name
  await router.push({ name: nextTab.name })
}
</script>

<template>
  <el-tabs
    v-model="editableTabsValue"
    type="card"
    closable
    @tab-remove="removeTab"
    @tab-click="clickTab"
  >
    <el-tab-pane
      v-for="item in editableTabs"
      :key="item.name"
      :label="item.title"
      :name="item.name"
    >
    </el-tab-pane>
  </el-tabs>
</template>
