import { defineStore } from 'pinia'
import type { Menu, Tab } from '@/type/entity'

export const displayStateStore = defineStore('displayStateStore', {
  state: () => {
    return {
      fix: document.body.clientWidth > 900 ? 'right' : false,
      showCatalogue: document.body.clientWidth > 900,
      expand: document.body.clientWidth > 900
    }
  }
})

export const loginStateStore = defineStore('loginStateStore', {
  state: () => {
    return {
      login: false
    }
  }
})

export const menuStore = defineStore('menuStore', {
  state: () => {
    return {
      menuList: [] as Menu[]
    }
  }
})

export const tabStore = defineStore('tabStore', {
  state: () => {
    return {
      editableTabs: [] as Tab[],
      editableTabsValue: ''
    }
  },
  actions: {
    addTab(tab: Tab) {
      const index = this.editableTabs.findIndex(e => e.name === tab.name)
      if (index === -1) {
        this.editableTabs.push({
          title: tab.title,
          name: tab.name
        })
      }
      this.editableTabsValue = tab.name
    },
  },
})

export const routeStore = defineStore('routeStore', {
  state: () => {
    return {
      hasRoute: false
    }
  }
})