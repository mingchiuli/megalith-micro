import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Button, Menu, Tab } from '@/type/entity'

/**
 * Menu store - dynamic menu tree from server
 */
export const menuStore = defineStore('menuStore', () => {
  const menuTree = ref<Menu>()
  return { menuTree }
})

/**
 * Button store - button permissions list
 */
export const buttonStore = defineStore('buttonStore', () => {
  const buttonList = ref<Button[]>([])
  return { buttonList }
})

/**
 * Tab store - tab management (add, remove, track active)
 */
export const tabStore = defineStore('tabStore', () => {
  const editableTabs = ref<Tab[]>([])
  const editableTabsValue = ref('')

  const addTab = (tab: Tab) => {
    const index = editableTabs.value.findIndex((e) => e.name === tab.name)
    if (index === -1) {
      editableTabs.value.push({
        title: tab.title,
        name: tab.name
      })
    }
    editableTabsValue.value = tab.name
  }

  const removeTab = (tabName: string) => {
    const index = editableTabs.value.findIndex((e) => e.name === tabName)
    if (index !== -1) {
      editableTabs.value.splice(index, 1)
    }
  }

  return { editableTabs, editableTabsValue, addTab, removeTab }
})