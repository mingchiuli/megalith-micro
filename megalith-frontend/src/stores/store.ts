import { defineStore } from 'pinia'
import type { Menu, Tab } from '@/type/entity'

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

export const routeStore = defineStore('routeStore', {
  state: () => {
    return {
      hasRoute: false
    }
  }
})