import { createRouter, createWebHistory } from 'vue-router'
import Intro from '@/views/Intro.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'intro',
      component: Intro
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/Blogs.vue')
    },
    {
      path: '/blogs',
      name: 'blogs',
      component: () => import('../views/Blogs.vue')
    },
    {
      path: '/blog/:id',
      name: 'blog',
      component: () => import('../views/Blog.vue')
    }
  ]
})

export default router
