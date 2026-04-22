import type { RouteRecordRaw } from 'vue-router'
import { UserType } from '@/types'

const adminRoutes: RouteRecordRaw = {
  path: '/admin',
  component: () => import('@/layouts/AdminLayout.vue'),
  meta: { requiresAuth: true, roles: [UserType.SCHOOL_ADMIN] },
  redirect: '/admin/dashboard',
  children: [
    {
      path: 'dashboard',
      name: 'AdminDashboard',
      component: () => import('@/views/admin/dashboard/index.vue'),
      meta: { title: '工作台', icon: 'HomeFilled', requiresAuth: true, roles: [UserType.SCHOOL_ADMIN], keepAlive: true },
    },
    {
      path: 'user',
      meta: { title: '用户管理', icon: 'User', requiresAuth: true, roles: [UserType.SCHOOL_ADMIN] },
      children: [
        {
          path: 'student',
          name: 'AdminStudentList',
          component: () => import('@/views/admin/user/StudentListView.vue'),
          meta: { title: '学生管理', permissions: ['perm:user:list'], keepAlive: true, requiresAuth: true, roles: [UserType.SCHOOL_ADMIN] },
        },
        {
          path: 'teacher',
          name: 'AdminTeacherList',
          component: () => import('@/views/admin/user/TeacherListView.vue'),
          meta: { title: '教师管理', permissions: ['perm:user:list'], keepAlive: true, requiresAuth: true, roles: [UserType.SCHOOL_ADMIN] },
        },
        {
          path: 'teaching-point',
          name: 'AdminTeachingPointList',
          component: () => import('@/views/admin/user/TeachingPointView.vue'),
          meta: { title: '教学点管理', permissions: ['perm:teaching-point:list'], keepAlive: true, requiresAuth: true, roles: [UserType.SCHOOL_ADMIN] },
        },
        {
          path: 'role',
          name: 'AdminRoleList',
          component: () => import('@/views/admin/user/RoleView.vue'),
          meta: { title: '角色权限', permissions: ['perm:role:list'], keepAlive: true, requiresAuth: true, roles: [UserType.SCHOOL_ADMIN] },
        },
      ],
    },
    {
      path: 'batch',
      meta: { title: '批次管理', icon: 'Calendar', requiresAuth: true, roles: [UserType.SCHOOL_ADMIN] },
      children: [
        {
          path: 'list',
          name: 'AdminBatchList',
          component: () => import('@/views/admin/batch/BatchListView.vue'),
          meta: { title: '批次列表', permissions: ['perm:batch:list'], keepAlive: true, requiresAuth: true, roles: [UserType.SCHOOL_ADMIN] },
        },
      ],
    },
    {
      path: 'paper',
      meta: { title: '论文管理', icon: 'Document', requiresAuth: true, roles: [UserType.SCHOOL_ADMIN] },
      children: [
        {
          path: 'relationship',
          name: 'AdminRelationship',
          component: () => import('@/views/admin/paper/RelationshipView.vue'),
          meta: { title: '指导关系', permissions: ['perm:relationship:manage'], keepAlive: true, requiresAuth: true, roles: [UserType.SCHOOL_ADMIN] },
        },
      ],
    },
  ],
}

export default adminRoutes
