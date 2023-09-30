import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import Intro from '@/views/Intro.vue'
import { GET } from '@/http/http'
import type { Menu } from '@/type/entity'
import { routeStore, menuStore, loginStateStore } from '@/stores/store'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/sys',
      name: 'system',
      component: () => import('@/views/System.vue'),
      children: []
    },
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
      component: () => import('@/views/Blogs.vue'),
      meta: {
        title: '登录'
      }
    },
    {
      path: '/blogs',
      name: 'blogs',
      component: () => import('@/views/Blogs.vue'),
      meta: {
        title: '内容列表'
      }
    },
    {
      path: '/blog/:id',
      name: 'blog',
      component: () => import('@/views/Blog.vue')
    }
  ]
})

router.beforeEach(async () => {
  if (!routeStore().hasRoute && loginStateStore().login) {
    const menus = await GET<Menu[]>('/sys/menu/nav')
    menus.forEach(menu => {
      menuStore().menuList.push(menu)
      const route = buildRoute(menu)
      router.addRoute(route)
    })
    routeStore().hasRoute = true
  }
})

//构建路由
const buildRoute = (menu: Menu): RouteRecordRaw => {
  let route = menuToRoute(menu)

  if (menu.children) {
    menu.children.forEach(childMenu => {
      const childRoute = buildRoute(childMenu)
      if (route.path) {
        route.children?.push(childRoute)
      } else {
        //找到sys的路由
        router.getRoutes()[1].children.push(childRoute)
      }

    })
  }
  return route
}

//导航转成路由
const menuToRoute = (menu: Menu): RouteRecordRaw => {
  //存在虚假的路由（只是父级菜单
  //真实的路由
  const route = {
    name: menu.name,
    path: menu.url,
    component: menu.component ? () => import(`@/views/${menu.component}.vue`) : null,
    children: [],
    meta: {
      icon: menu.icon,
      title: menu.title
    }
  } as RouteRecordRaw
  return route
}

export default router
