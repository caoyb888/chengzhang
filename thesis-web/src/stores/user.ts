import { defineStore } from 'pinia'
import type { LoginUser, LoginVO, UserType } from '@/types'
import { authApi } from '@/api/auth'
import { useAppStore } from './app'

interface UserState {
  accessToken: string
  refreshToken: string
  userInfo: LoginUser | null
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    accessToken: localStorage.getItem('thesis_access_token') ?? '',
    refreshToken: localStorage.getItem('thesis_refresh_token') ?? '',
    userInfo: (() => {
      const raw = localStorage.getItem('thesis_user')
      return raw ? (JSON.parse(raw) as LoginUser) : null
    })(),
  }),

  getters: {
    isLoggedIn: (state) => !!state.accessToken && !!state.userInfo,
    permissions: (state) => state.userInfo?.permissions ?? [],
    userType: (state) => state.userInfo?.userType,
    userId: (state) => state.userInfo?.userId ?? '',
    schoolId: (state) => state.userInfo?.schoolId ?? '',
    teachingPointId: (state) => state.userInfo?.teachingPointId,
    realName: (state) => state.userInfo?.realName ?? '',

    isSchoolAdmin: (state) => state.userInfo?.userType === 'SCHOOL_ADMIN',
    isPointAdmin: (state) => state.userInfo?.userType === 'POINT_ADMIN',
    isTeacher: (state) => state.userInfo?.userType === 'TEACHER',
    isAssistant: (state) => state.userInfo?.userType === 'ASSIST_TEACHER',
    isStudent: (state) => state.userInfo?.userType === 'STUDENT',

    homeRoute: (state): string => {
      const map: Record<string, string> = {
        SCHOOL_ADMIN: '/admin/dashboard',
        POINT_ADMIN: '/point/dashboard',
        TEACHER: '/teacher/dashboard',
        ASSIST_TEACHER: '/assist/dashboard',
        STUDENT: '/student/dashboard',
      }
      return state.userInfo ? map[state.userInfo.userType] ?? '/login' : '/login'
    },
  },

  actions: {
    setTokens(accessToken: string, refreshToken: string) {
      this.accessToken = accessToken
      this.refreshToken = refreshToken
      localStorage.setItem('thesis_access_token', accessToken)
      localStorage.setItem('thesis_refresh_token', refreshToken)
    },

    setUserInfo(userInfo: LoginUser) {
      this.userInfo = userInfo
      localStorage.setItem('thesis_user', JSON.stringify(userInfo))
    },

    async loginByPassword(username: string, password: string) {
      const res = await authApi.loginByPassword({ username, password, clientType: 'WEB' })
      this.setTokens(res.accessToken, res.refreshToken)
      this.setUserInfo(res.userInfo)
      useAppStore().applyRoleDefaultTheme(res.userInfo.userType)
    },

    async loginBySms(phone: string, smsCode: string) {
      const res = await authApi.loginBySms({ phone, smsCode, clientType: 'WEB' })
      this.setTokens(res.accessToken, res.refreshToken)
      this.setUserInfo(res.userInfo)
      useAppStore().applyRoleDefaultTheme(res.userInfo.userType)
    },

    async logout() {
      try { await authApi.logout() } catch { /* ignore */ }
      this.clearAuth()
    },

    clearAuth() {
      this.accessToken = ''
      this.refreshToken = ''
      this.userInfo = null
      localStorage.removeItem('thesis_access_token')
      localStorage.removeItem('thesis_refresh_token')
      localStorage.removeItem('thesis_user')
    },

    hasPermission(perm: string): boolean {
      return this.permissions.includes(perm)
    },

    hasAnyPermission(perms: string[]): boolean {
      return perms.some((p) => this.permissions.includes(p))
    },
  },
})
