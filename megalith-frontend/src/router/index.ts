import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import Intro from '@/views/intro.vue'
import { GET } from '@/http/http'
import type { Menu } from '@/type/entity'
import { routeStore, menuStore, loginStateStore } from '@/stores/store'

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
      component: () => import('@/views/blogs.vue'),
      meta: {
        title: '登录'
      }
    },
    {
      path: '/blogs',
      name: 'blogs',
      component: () => import('@/views/blogs.vue'),
      meta: {
        title: '内容列表'
      }
    },
    {
      path: '/blog/:id',
      name: 'blog',
      component: () => import('@/views/blog.vue')
    },
    {
      path: '/:catchAll(.*)',
      component: () => import('@/views/404.vue')
    }
  ]
})

router.beforeEach(async (to, _from, next) => {
  if (localStorage.getItem('accessToken')) {
    loginStateStore().login = true
  }

  if (!routeStore().hasRoute && loginStateStore().login) {
    const menus = await GET<Menu[]>('/sys/menu/nav')
    const systemRoute = {
      path: '/sys',
      name: 'system',
      component: () => import('@/views/system.vue'),
      children: []
    } as RouteRecordRaw

    menus.forEach(menu => {
      menuStore().menuList.push(menu)
      const route = buildRoute(menu, systemRoute)
      if (route.path) {
        systemRoute.children?.push(route)
      }
    })
    router.addRoute(systemRoute)
    routeStore().hasRoute = true
  }

  if (to.meta.title) {
    document.title = to.meta.title as string
  }
  next()
})

//构建路由
const buildRoute = (menu: Menu, systemRoute: RouteRecordRaw): RouteRecordRaw => {
  let route = menuToRoute(menu)
  
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
