import { loginStateStore, routeStore, menuStore, tabStore } from '@/stores/store'

export function clearLoginState() {
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('userinfo')
  loginStateStore().login = false
  routeStore().hasRoute = false 
  menuStore().menuList = []
  tabStore().editableTabs = []
  tabStore().editableTabsValue = ''
}
