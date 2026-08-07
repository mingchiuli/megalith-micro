import {
  createMemoryHistory,
  createRouter,
  createWebHistory,
  type RouteLocationNormalized,
  type RouteRecordRaw,
  type Router
} from 'vue-router'
import { storeToRefs } from 'pinia'
import { RoutesEnum, type Button, type Menu, type MenuNode, type Tab, type UserInfo } from '@/type/entity'
import {
  menuStore,
  loginStateStore,
  welcomeStateStore,
  buttonStore,
  tabStore,
  authMarkStore
} from '@/stores'
import { diff, findMenuByPath } from '@/utils/common'
import { API_ENDPOINTS } from '@/config/apiConfig'
import type { ApiClient } from '@/http/http'

const modules = import.meta.glob('@/views/sys/*.vue')

type RouterOptions = {
  server: boolean
  api: ApiClient
}

const publicRoutes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'intro',
    component: () => import('@/views/IntroView.vue'),
    meta: { titleKey: 'admin.intro' }
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/LoginView.vue'),
    meta: { titleKey: 'auth.login' }
  },
  {
    path: '/register/:token',
    name: 'register',
    component: () => import('@/views/RegisterView.vue'),
    meta: { titleKey: 'admin.register' }
  },
  {
    path: '/blogs',
    name: 'blogs',
    component: () => import('@/views/BlogsView.vue'),
    meta: { titleKey: 'admin.contentList' }
  },
  {
    path: '/blog/:id',
    name: 'blog',
    component: () => import('@/views/BlogView.vue')
  },
  {
    path: '/:catchAll(.*)',
    name: 'not-found',
    component: () => import('@/views/404View.vue'),
    meta: { status: 404 }
  }
]

export const createAppRouter = ({ server, api }: RouterOptions): Router => {
  const router = createRouter({
    history: server
      ? createMemoryHistory(import.meta.env.BASE_URL)
      : createWebHistory(import.meta.env.BASE_URL),
    routes: publicRoutes
  })

  const getSession = () =>
    Promise.all([
      api.GET<Menu>(API_ENDPOINTS.AUTH.MENU_NAV),
      api.GET<UserInfo>(API_ENDPOINTS.AUTH.USER_INFO)
    ])

  router.beforeEach(async (to) => {
    const privateRoute = to.path.startsWith('/sys') || to.path.startsWith('/backend')
    if (to.path.startsWith('/sys')) welcomeStateStore().welcomeBackend = false

    if (!loginStateStore().login) {
      return privateRoute ? { name: 'login', query: { redirect: to.fullPath } } : undefined
    }

    try {
      let menuTree = menuStore().menuTree
      const restoredSession = authMarkStore().auth && menuTree && loginStateStore().user
      if (!restoredSession) {
        const [fetchedMenu, user] = await getSession()
        menuTree = fetchedMenu
        loginStateStore().user = user
      }
      if (!menuTree) throw new Error('Authenticated menu is unavailable')

      const addedRoute = applyMenuTree(router, menuTree)
      dealSysTab(to, menuTree)
      authMarkStore().auth = true
      if (to.path.startsWith('/login')) return { name: 'blogs' }
      if (addedRoute && (!to.name || to.name === 'not-found')) return to.fullPath
    } catch {
      clearAuthStores(router)
      if (privateRoute) return { name: 'login', query: { redirect: to.fullPath } }
    }
  })

  return router
}

export const clearAuthStores = (router: Router) => {
  const rootName = menuStore().menuTree?.name
  if (rootName && router.hasRoute(rootName)) router.removeRoute(rootName)
  authMarkStore().auth = false
  loginStateStore().login = false
  loginStateStore().user = undefined
  menuStore().menuTree = undefined
  buttonStore().buttonList = []
  tabStore().editableTabs = []
  tabStore().editableTabsValue = ''
}

const dealSysTab = (to: RouteLocationNormalized, menuTree: Menu) => {
  if (!to.path.startsWith('/sys')) return
  const menu = findMenuByPath(menuTree.children, to.path)
  if (menu) tabStore().addTab({ name: menu.name, title: menu.title })
}

const applyMenuTree = (router: Router, rootMenu: Menu): boolean => {
  const buttons = collectButtons(rootMenu)
  const { buttonList } = storeToRefs(buttonStore())
  if (diff(buttonList.value, buttons)) buttonList.value = buttons

  const { menuTree } = storeToRefs(menuStore())
  menuTree.value = rootMenu

  if (router.hasRoute(rootMenu.name)) {
    return false
  }
  router.addRoute(buildRoute(rootMenu))
  return true
}

const collectButtons = (rootMenu: MenuNode): Button[] => {
  const buttons: Button[] = []
  const walk = (node: MenuNode) => {
    if (node.type === RoutesEnum.BUTTON) {
      buttons.push(node)
      return
    }
    node.children.forEach(walk)
  }
  walk(rootMenu)
  return buttons
}

const buildRoute = (menu: Menu): RouteRecordRaw => {
  const route = menuToRoute(menu)
  menu.children.forEach((child) => {
    if (child.type !== RoutesEnum.BUTTON) route.children?.push(buildRoute(child))
  })
  return route
}

const menuToRoute = (menu: Menu): RouteRecordRaw => ({
  name: menu.name,
  path: menu.url,
  children: [],
  component: modules[`/src/views/${menu.component}.vue`],
  meta: {
    icon: menu.icon,
    title: menu.title
  }
})

export type { Tab }
