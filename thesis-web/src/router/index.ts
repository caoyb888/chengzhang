import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { UserType } from '@/types'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { public: true },
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, roles: [UserType.SCHOOL_ADMIN] },
    children: [
      {
        path: '',
        redirect: '/admin/dashboard',
      },
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
      },
    ],
  },
  {
    path: '/point',
    component: () => import('@/layouts/PointLayout.vue'),
    meta: { requiresAuth: true, roles: [UserType.POINT_ADMIN] },
    children: [
      {
        path: '',
        redirect: '/point/dashboard',
      },
      {
        path: 'dashboard',
        name: 'PointDashboard',
        component: () => import('@/views/point/Dashboard.vue'),
      },
    ],
  },
  {
    path: '/teacher',
    component: () => import('@/layouts/TeacherLayout.vue'),
    meta: { requiresAuth: true, roles: [UserType.TEACHER, UserType.ASSIST_TEACHER] },
    children: [
      {
        path: '',
        redirect: '/teacher/papers',
      },
      {
        path: 'papers',
        name: 'TeacherPapers',
        component: () => import('@/views/teacher/Papers.vue'),
      },
    ],
  },
  {
    path: '/student',
    component: () => import('@/layouts/StudentLayout.vue'),
    meta: { requiresAuth: true, roles: [UserType.STUDENT] },
    children: [
      {
        path: '',
        redirect: '/student/paper',
      },
      {
        path: 'paper',
        name: 'StudentPaper',
        component: () => import('@/views/student/Paper.vue'),
      },
    ],
  },
  {
    path: '/',
    redirect: () => {
      const userStore = useUserStore()
      return roleHomePath(userStore.userType)
    },
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
  },
]

function roleHomePath(userType: UserType | undefined): string {
  switch (userType) {
    case UserType.SCHOOL_ADMIN:
      return '/admin'
    case UserType.POINT_ADMIN:
      return '/point'
    case UserType.TEACHER:
    case UserType.ASSIST_TEACHER:
      return '/teacher'
    case UserType.STUDENT:
      return '/student'
    default:
      return '/login'
  }
}

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

router.beforeEach((to) => {
  const userStore = useUserStore()

  if (to.meta.public) return true

  if (!userStore.isLoggedIn) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  const allowedRoles = to.meta.roles as UserType[] | undefined
  if (allowedRoles && userStore.userType && !allowedRoles.includes(userStore.userType)) {
    return roleHomePath(userStore.userType)
  }

  return true
})

export default router
