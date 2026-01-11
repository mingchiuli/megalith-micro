import { defineStore } from 'pinia'
import type { Button, Menu, Tab } from '@/type/entity'
import { ref } from 'vue'

export const welcomeStateStore = defineStore('welcomeStateStore', () => {
  const welcomeBackend = ref(true)
  return { welcomeBackend }
})

export const loginStateStore = defineStore('loginStateStore', () => {
  const login = ref(false)
  return { login }
})

export const authMarkStore = defineStore('authMarkStore', () => {
  const auth = ref(false)
  return { auth }
})

export const menuStore = defineStore('menuStore', () => {
  const menuTree = ref<Menu>()
  return { menuTree }
})

export const buttonStore = defineStore('buttonStore', () => {
  const buttonList = ref<Button[]>([])
  return { buttonList }
})

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

  return { editableTabs, editableTabsValue, addTab }
})

export const blogsStore = defineStore('blogsPageStore', () => {
  const pageNum = ref(1)
  const searchPageNum = ref(1)
  const keywords = ref('')
  return { pageNum, searchPageNum, keywords }
})

export const pageStore = defineStore('pageStore', () => {
  const front = ref(true)
  return { front }
})

export const themeStore = defineStore('themeStore', () => {
  // 从 localStorage 读取主题设置，默认为 light
  const isDark = ref(localStorage.getItem('theme') === 'dark')

  const toggleTheme = () => {
    isDark.value = !isDark.value
    const htmlElement = document.documentElement

    if (isDark.value) {
      htmlElement.classList.add('dark')
      localStorage.setItem('theme', 'dark')
    } else {
      htmlElement.classList.remove('dark')
      localStorage.setItem('theme', 'light')
    }
  }

  // 初始化时应用主题
  const initTheme = () => {
    const htmlElement = document.documentElement
    if (isDark.value) {
      htmlElement.classList.add('dark')
    } else {
      htmlElement.classList.remove('dark')
    }
  }

  return { isDark, toggleTheme, initTheme }
})