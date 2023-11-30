import { defineStore } from 'pinia'
import type { Menu, Tab } from '@/type/entity'
import { reactive, ref } from 'vue'


export const displayStateStore = defineStore('displayStateStore', () => {
  const fix = ref(document.body.clientWidth > 900 ? 'right' : false)
  const showCatalogue = ref(false)
  const expand = ref(document.body.clientWidth > 900)
  const updateShowCatalogue = () => {
    if (document.body.clientWidth > 900) {
      showCatalogue.value = true
    }
  }
  return { fix, showCatalogue, expand, updateShowCatalogue }
})

export const loginStateStore = defineStore('loginStateStore', () => {
  const login = ref(false)
  return { login }
})

export const menuStore = defineStore('menuStore', () => {
  const menuList = reactive<Menu[]>([])
  return { menuList }
})


export const tabStore = defineStore('tabStore', () => {
  const editableTabs = reactive<Tab[]>([])
  const editableTabsValue = ref('')
  const addTab = (tab: Tab) => {
    const index = editableTabs.findIndex(e => e.name === tab.name)
      if (index === -1) {
        editableTabs.push({
          title: tab.title,
          name: tab.name
        })
      }
      editableTabsValue.value = tab.name
  }

  return { editableTabs, editableTabsValue, addTab }
})

export const routeStore = defineStore('routeStore', () => {
  const hasRoute = ref(false)
  return { hasRoute }
})

export const blogsPageNumStore = defineStore('blogsPageNumStore', () => {
  const value = ref(1)
  return { value }
})