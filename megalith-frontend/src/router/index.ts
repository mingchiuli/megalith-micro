import { createRouter, createWebHistory, type RouteLocationNormalized, type RouteRecordRaw } from 'vue-router'
import { GET } from '@/http/http'
import { type Menu, type MenusAndButtons, type Tab } from '@/type/entity'
import { menuStore, loginStateStore, welcomeStateStore, buttonStore, tabStore } from '@/stores/store'
import { storeToRefs } from 'pinia'
import { diff, findMenuByPath } from '@/utils/tools'

const modules = import.meta.glob('@/views/sys/*.vue')

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'intro',
      component: () => import('@/views/IntroView.vue'),
      meta: {
        title: '简介'
      }
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: {
        title: '登录'
      }
    },
    {
      path: '/register/:token',
      name: 'register',
      component: () => import('@/views/RegisterView.vue'),
      meta: {
        title: '注册'
      }
    },
    {
      path: '/blogs',
      name: 'blogs',
      component: () => import('@/views/BlogsView.vue'),
      meta: {
        title: '内容列表'
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

router.beforeEach(async (to, _from, next) => {
  if (to.path.startsWith('/sys')) {
    welcomeStateStore().welcomeBackend = false
  }

  if (localStorage.getItem('accessToken') && !loginStateStore().login) {
    loginStateStore().login = true
  }

  if (to.path.startsWith('/login') && loginStateStore().login) {
    router.push({
      name: 'blogs'
    })
  }

  if (to.meta.title) {
    document.title = to.meta.title as string
  }

  if (loginStateStore().login) {
    let allKindsInfo: MenusAndButtons
    //页面被手动刷新
    if (!to.name || !router.hasRoute(to.name)) {
      allKindsInfo = await GET<MenusAndButtons>('/auth/menu/nav')
      callBackRequireRoutes(allKindsInfo)
      console.log(router.getRoutes())
      console.log(router.hasRoute(to.path))
      dealSysTab(to, allKindsInfo)
      //重定向解决刷新404
      next(to)
    } else {
      //正常路由切换diff
      GET<MenusAndButtons>('/auth/menu/nav').then(resp => {
        allKindsInfo = resp
        callBackRequireRoutes(allKindsInfo)
        dealSysTab(to, allKindsInfo)
      })
      next()
    }
  } else {
    next()
  }
})

const dealSysTab = (to: RouteLocationNormalized, allKindsInfo: MenusAndButtons) => {
  //处理tab
  if (to.path.startsWith('/sys')) {
    const menu = findMenuByPath(allKindsInfo.menus, to.path)
    if (menu) {
      const tab: Tab = { "name": menu.name, "title": menu.title }
      tabStore().addTab(tab)
    }
  }
}

const callBackRequireRoutes = (allKindsInfo: MenusAndButtons) => {
  const buttons = allKindsInfo.buttons
  const { buttonList } = storeToRefs(buttonStore())
  const difButton = diff(buttonList.value, buttons)
  if (difButton) {
    buttonList.value = []
    buttons.forEach(button => buttonList.value.push(button))
  }

  const systemRoute = {
    path: '/backend',
    name: 'system',
    component: () => import('@/views/SystemView.vue'),
    children: []
  } as RouteRecordRaw

  const { menuList } = storeToRefs(menuStore())
  const menus = allKindsInfo.menus
  const difMenu = diff(menuList.value, menus)
  if (difMenu) {
    menuList.value = []
    if (router.hasRoute('system')) {
      router.removeRoute('system')
    }
    menus
      .forEach(menu => {
        menuList.value.push(menu)
        const route = buildRoute(menu, systemRoute)
        if (route.path) {
          systemRoute.children?.push(route)
        }
      })

    router.addRoute(systemRoute)
  }
}

//构建路由
const buildRoute = (menu: Menu, systemRoute: RouteRecordRaw): RouteRecordRaw => {
  const route = menuToRoute(menu)

  menu.children?.forEach(childMenu => {
    const childRoute = buildRoute(childMenu, systemRoute)
    if (route.path) {
      route.children?.push(childRoute)
    } else {
      //找到sys的路由
      systemRoute.children?.push(childRoute)
    }
  })
  return route
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
