import type { RouteRecordRaw } from 'vue-router'

const staticRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', public: true, hideInMenu: true },
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '无权限', public: true, hideInMenu: true },
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '页面不存在', public: true, hideInMenu: true },
  },
]

export default staticRoutes
