import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { usePermissionStore } from '@/stores/permission'
import { UserType } from '@/types'
import staticRoutes from './static.routes'
import adminRoutes from './modules/admin.routes'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', redirect: '/login' },
    ...staticRoutes,
    adminRoutes,
    {
      path: '/point',
      component: () => import('@/layouts/PointLayout.vue'),
      meta: { requiresAuth: true, roles: [UserType.POINT_ADMIN] },
      redirect: '/point/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'PointDashboard',
          component: () => import('@/views/point/Dashboard.vue'),
          meta: { title: '工作台', icon: 'HomeFilled', requiresAuth: true, roles: [UserType.POINT_ADMIN], keepAlive: true },
        },
        {
          path: 'student',
          name: 'PointStudentList',
          component: () => import('@/views/point/student/StudentListView.vue'),
          meta: { title: '学生管理', icon: 'User', requiresAuth: true, roles: [UserType.POINT_ADMIN], keepAlive: true },
        },
        {
          path: 'teacher',
          name: 'PointAssistTeacherList',
          component: () => import('@/views/point/teacher/AssistTeacherListView.vue'),
          meta: { title: '辅助教师', icon: 'UserFilled', requiresAuth: true, roles: [UserType.POINT_ADMIN], keepAlive: true },
        },
        {
          path: 'relationship',
          name: 'PointRelationship',
          component: () => import('@/views/point/relationship/RelationshipView.vue'),
          meta: { title: '指导关系', icon: 'Connection', requiresAuth: true, roles: [UserType.POINT_ADMIN], keepAlive: true },
        },
      ],
    },
    {
      path: '/teacher',
      component: () => import('@/layouts/TeacherLayout.vue'),
      meta: { requiresAuth: true, roles: [UserType.TEACHER, UserType.ASSIST_TEACHER] },
      redirect: '/teacher/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'TeacherDashboard',
          component: () => import('@/views/teacher/Dashboard.vue'),
          meta: { title: '工作台', icon: 'HomeFilled', requiresAuth: true, roles: [UserType.TEACHER, UserType.ASSIST_TEACHER], keepAlive: true },
        },
        {
          path: 'papers',
          name: 'TeacherPapers',
          component: () => import('@/views/teacher/Papers.vue'),
          meta: { title: '论文审核', icon: 'Document', requiresAuth: true, roles: [UserType.TEACHER, UserType.ASSIST_TEACHER], keepAlive: true },
        },
      ],
    },
    {
      path: '/student',
      component: () => import('@/layouts/StudentLayout.vue'),
      meta: { requiresAuth: true, roles: [UserType.STUDENT] },
      redirect: '/student/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'StudentDashboard',
          component: () => import('@/views/student/Dashboard.vue'),
          meta: { title: '论文进度', icon: 'Compass', requiresAuth: true, roles: [UserType.STUDENT], keepAlive: true },
        },
        {
          path: 'paper',
          name: 'StudentPaper',
          component: () => import('@/views/student/Paper.vue'),
          meta: { title: '我的论文', icon: 'Document', requiresAuth: true, roles: [UserType.STUDENT], keepAlive: true },
        },
      ],
    },
  ],
  scrollBehavior: () => ({ top: 0 }),
})

router.beforeEach((to) => {
  const userStore = useUserStore()
  const permissionStore = usePermissionStore()

  if (to.meta.public) return true

  if (!userStore.isLoggedIn) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  const allowedRoles = to.meta.roles as UserType[] | undefined
  if (allowedRoles && userStore.userType && !allowedRoles.includes(userStore.userType as UserType)) {
    return userStore.homeRoute
  }

  const requiredPerms = to.meta.permissions as string[] | undefined
  if (requiredPerms && !permissionStore.hasAllPermissions(requiredPerms)) {
    return '/403'
  }

  return true
})

export default router
