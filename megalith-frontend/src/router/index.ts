import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import Intro from '@/views/IntroView.vue'
import { GET } from '@/http/http'
import { RoutesEnum, type Menu } from '@/type/entity'
import { menuStore, loginStateStore, displayStateStore } from '@/stores/store'
import { storeToRefs } from 'pinia'

const modules = import.meta.glob('@/views/sys/*.vue')

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'intro',
      component: Intro,
      meta: {
        title: '简介'
      }
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/BlogsView.vue'),
      meta: {
        title: '登录'
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
    displayStateStore().welcomeBackend = false
  }

  if (localStorage.getItem('accessToken') && !loginStateStore().login) {
    loginStateStore().login = true
  }

  if (loginStateStore().login) {
    const menus = await GET<Menu[]>('/sys/menu/nav')
    const systemRoute = {
      path: '/backend',
      name: 'system',
      component: () => import('@/views/SystemView.vue'),
      children: []
    } as RouteRecordRaw

    const { menuList } = storeToRefs(menuStore())
    menuList.value = []
    menus
      .filter(item => RoutesEnum.BUTTON !== item.type)
      .forEach(menu => {
        menuList.value.push(menu)
        const route = buildRoute(menu, systemRoute)
        if (route.path) {
          systemRoute.children?.push(route)
        }
      })
    router.addRoute(systemRoute)
  }

  if (to.meta.title) {
    document.title = to.meta.title as string
  }
  next()
})

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
