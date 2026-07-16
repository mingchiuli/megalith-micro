import type { RouteLocationNormalized, RouteRecordRaw } from 'vue-router'
import { GET } from '@/http/http'
import { RoutesEnum, type Button, type Menu, type MenuNode, type Tab } from '@/type/entity'
import {
  menuStore,
  loginStateStore,
  welcomeStateStore,
  buttonStore,
  tabStore,
  authMarkStore
} from '@/stores'
import { diff, findMenuByPath } from '@/utils/tools'
import { API_ENDPOINTS } from '@/config/apiConfig'
import { storage } from '@/utils/storage'

const modules = import.meta.glob('@/views/sys/*.vue')

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'intro',
      component: () => import('@/views/IntroView.vue'),
      meta: {
        titleKey: 'admin.intro'
      }
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: {
        titleKey: 'auth.login'
      }
    },
    {
      path: '/register/:token',
      name: 'register',
      component: () => import('@/views/RegisterView.vue'),
      meta: {
        titleKey: 'admin.register'
      }
    },
    {
      path: '/blogs',
      name: 'blogs',
      component: () => import('@/views/BlogsView.vue'),
      meta: {
        titleKey: 'admin.contentList'
      }
    },
    {
      path: '/blog/:id',
      name: 'blog',
      component: () => import('@/views/BlogView.vue')
    },
    {
      path: '/:catchAll(.*)',
      component: () => import('@/views/404View.vue')
    }
  ]
})

router.beforeEach(async (to) => {
  if (to.path.startsWith('/sys')) {
    welcomeStateStore().welcomeBackend = false
  }

  if (storage.isLoggedIn() && !loginStateStore().login) {
    loginStateStore().login = true
  }

  if (to.path.startsWith('/login') && loginStateStore().login) {
    return {
      name: 'blogs'
    }
  }

  if (to.meta.title) {
    document.title = to.meta.title as string
  }

  if (loginStateStore().login) {
    let menuTree: Menu
    //页面被手动刷新
    if (!to.name || !router.hasRoute(to.name)) {
      if (!authMarkStore().auth) {
        authMarkStore().auth = true
        menuTree = await GET<Menu>(API_ENDPOINTS.AUTH.MENU_NAV)
        callBackRequireRoutes(menuTree)
        dealSysTab(to, menuTree)
        return to.fullPath
      }
    } else {
      //正常路由切换diff
      GET<Menu>(API_ENDPOINTS.AUTH.MENU_NAV).then((resp) => {
        menuTree = resp
        callBackRequireRoutes(menuTree)
        dealSysTab(to, menuTree)
      })
    }
  }
})

const dealSysTab = (to: RouteLocationNormalized, menuTree: Menu) => {
  //处理tab
  if (to.path.startsWith('/sys')) {
    const menu = findMenuByPath(menuTree.children, to.path)
    if (menu) {
      const tab: Tab = { name: menu.name, title: menu.title }
      tabStore().addTab(tab)
    }
  }
}

const callBackRequireRoutes = (rootMenu: Menu) => {
  const buttons = collectButtons(rootMenu)
  const { buttonList } = storeToRefs(buttonStore())
  const difButton = diff(buttonList.value, buttons)
  if (difButton) {
    buttonList.value = []
    buttons.forEach((button) => buttonList.value.push(button))
  }

  const { menuTree } = storeToRefs(menuStore())

  if (menuTree.value) {
    const difMenu = diff([menuTree.value], [rootMenu])
    if (difMenu) {
      menuTree.value = rootMenu
    }
  } else {
    menuTree.value = rootMenu
  }

  const rootName = rootMenu.name
  if (router.hasRoute(rootName)) {
    router.removeRoute(rootName)
  }

  const rootRoute = buildRoute(rootMenu)
  router.addRoute(rootRoute)
}

const collectButtons = (rootMenu: MenuNode): Button[] => {
  const buttons: Button[] = []

  const walk = (node: MenuNode) => {
    if (isButtonNode(node)) {
      buttons.push(node)
      return
    }

    node.children.forEach(walk)
  }

  walk(rootMenu)
  return buttons
}

const isButtonNode = (node: MenuNode): node is Button => node.type === RoutesEnum.BUTTON
const isRouteMenuNode = (node: MenuNode): node is Menu => node.type !== RoutesEnum.BUTTON

//构建路由
const buildRoute = (rootMenu: Menu): RouteRecordRaw => {
  const rootRoute = menuToRoute(rootMenu)

  rootMenu.children?.forEach((childMenu) => {
    if (!isRouteMenuNode(childMenu)) {
      return
    }

    const childRoute = buildRoute(childMenu)
    rootRoute.children?.push(childRoute)
  })
  return rootRoute
}

//导航转成路由
const menuToRoute = (menu: Menu): RouteRecordRaw => {
  //存在虚假的路由（只是父级菜单
  //真实的路由
  const route = {
    name: menu.name,
    path: menu.url,
    children: [],
    component: modules[`/src/views/${menu.component}.vue`],
    meta: {
      icon: menu.icon,
      title: menu.title
    }
  } as RouteRecordRaw

  return route
}

export default router
