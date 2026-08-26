import {
  createMemoryHistory,
  createRouter,
  createWebHistory,
  type RouteLocationNormalized,
  type RouteRecordRaw,
  type Router
} from 'vue-router'
import { storeToRefs, type Pinia } from 'pinia'
import { RoutesEnum, type Button, type Menu, type MenuNode, type UserInfo } from '@/type/entity'
import {
  menuStore,
  loginStateStore,
  buttonStore,
  tabStore,
  authMarkStore,
  ssrDataStore,
  protectedBlogStore
} from '@/stores'
import { diff, findMenuByPath } from '@/utils/common'
import { API_ENDPOINTS } from '@/config/apiConfig'
import type { ApiClient } from '@/http/http'

const modules = import.meta.glob('@/views/sys/*.vue')

type RouterOptions = {
  server: boolean
  api: ApiClient
  pinia: Pinia
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

export const createAppRouter = ({ server, api, pinia }: RouterOptions): Router => {
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
  let rematchingPath: string | undefined
  let latestNavigationRequest = 0

  const refreshNavigation = () => {
    const request = ++latestNavigationRequest
    void api
      .GET<Menu>(API_ENDPOINTS.AUTH.MENU_NAV)
      .then((menuTree) => {
        if (request !== latestNavigationRequest) return
        if (!loginStateStore(pinia).login || !authMarkStore(pinia).auth) return
        applyMenuTree(router, menuTree, pinia)
      })
      .catch(() => undefined)
  }

  router.beforeEach(async (to) => {
    const privateRoute = to.path.startsWith('/sys') || to.path.startsWith('/backend')
    const loginState = loginStateStore(pinia)
    const isRouteRematch = rematchingPath === to.fullPath
    rematchingPath = undefined

    if (!loginState.login && !privateRoute) return

    try {
      let menuTree = menuStore(pinia).menuTree
      const restoredSession = authMarkStore(pinia).auth && menuTree && loginState.user
      if (!restoredSession) {
        latestNavigationRequest += 1
        const [fetchedMenu, user] = await getSession()
        menuTree = fetchedMenu
        loginState.user = user
      }
      if (!menuTree) throw new Error('Authenticated menu is unavailable')

      const addedRoute = applyMenuTree(router, menuTree, pinia)
      dealSysTab(to, menuTree, pinia)
      loginState.login = true
      authMarkStore(pinia).auth = true
      if (to.path.startsWith('/login')) return { name: 'blogs' }
      if (privateRoute && restoredSession && !isRouteRematch) refreshNavigation()
      if (addedRoute && (!to.name || to.name === 'not-found')) {
        rematchingPath = to.fullPath
        return to.fullPath
      }
    } catch {
      clearAuthStores(router, pinia)
      if (privateRoute) return { name: 'login', query: { redirect: to.fullPath } }
    }
  })

  return router
}

export const clearAuthStores = (router: Router, pinia: Pinia) => {
  const rootName = menuStore(pinia).menuTree?.name
  if (rootName && router.hasRoute(rootName)) router.removeRoute(rootName)
  authMarkStore(pinia).auth = false
  loginStateStore(pinia).login = false
  loginStateStore(pinia).user = undefined
  menuStore(pinia).menuTree = undefined
  buttonStore(pinia).buttonList = []
  tabStore(pinia).editableTabs = []
  tabStore(pinia).editableTabsValue = ''
  ssrDataStore(pinia).clear()
  protectedBlogStore(pinia).clear()
  loginStateStore(pinia).clearSessionExpired()
}

const dealSysTab = (to: RouteLocationNormalized, menuTree: Menu, pinia: Pinia) => {
  if (!to.path.startsWith('/sys')) return
  const menu = findMenuByPath(menuTree.children, to.path)
  if (menu) tabStore(pinia).addTab({ name: menu.name, title: menu.title })
}

const applyMenuTree = (router: Router, rootMenu: Menu, pinia: Pinia): boolean => {
  const currentMenu = menuStore(pinia).menuTree
  const menuChanged = !currentMenu || diff([currentMenu], [rootMenu])
  const buttons = collectButtons(rootMenu)
  const { buttonList } = storeToRefs(buttonStore(pinia))
  if (diff(buttonList.value, buttons)) buttonList.value = buttons

  const { menuTree } = storeToRefs(menuStore(pinia))
  if (menuChanged) menuTree.value = rootMenu

  if (currentMenu?.name !== rootMenu.name && currentMenu && router.hasRoute(currentMenu.name)) {
    router.removeRoute(currentMenu.name)
  }
  const addedRoute = !router.hasRoute(rootMenu.name)
  if (menuChanged || addedRoute) {
    if (!addedRoute) router.removeRoute(rootMenu.name)
    router.addRoute(buildRoute(rootMenu))
  }
  return addedRoute
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

const buildRoute = (
  menu: Menu,
  layout: 'system' | undefined = menu.component === 'sys/SystemView' ? 'system' : undefined
): RouteRecordRaw => {
  const route = menuToRoute(menu, layout)
  menu.children.forEach((child) => {
    if (child.type !== RoutesEnum.BUTTON) route.children?.push(buildRoute(child, layout))
  })
  return route
}

const menuToRoute = (menu: Menu, layout?: 'system'): RouteRecordRaw => ({
  name: menu.name,
  path: menu.url,
  children: [],
  component: modules[`/src/views/${menu.component}.vue`],
  meta: {
    icon: menu.icon,
    title: menu.title,
    ...(layout ? { layout } : {})
  }
})
