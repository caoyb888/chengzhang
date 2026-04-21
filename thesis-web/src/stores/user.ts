import { defineStore } from 'pinia'
import type { LoginUser } from '@/types'
import { useAppStore } from './app'

interface UserState {
  token: string | null
  userInfo: LoginUser | null
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    token: localStorage.getItem('thesis_token'),
    userInfo: (() => {
      const raw = localStorage.getItem('thesis_user')
      return raw ? (JSON.parse(raw) as LoginUser) : null
    })(),
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    permissions: (state) => state.userInfo?.permissions ?? [],
    userType: (state) => state.userInfo?.userType,
  },

  actions: {
    async loginByPassword(username: string, password: string) {
      const { default: request } = await import('@/utils/request')
      const res = await request.post<{ token: string; userInfo: LoginUser }>('/auth/login', {
        username,
        password,
      })
      this.token = res.token
      this.userInfo = res.userInfo
      localStorage.setItem('thesis_token', res.token)
      localStorage.setItem('thesis_user', JSON.stringify(res.userInfo))
      useAppStore().applyRoleDefaultTheme(res.userInfo.userType)
    },

    logout() {
      this.token = null
      this.userInfo = null
      localStorage.removeItem('thesis_token')
      localStorage.removeItem('thesis_user')
    },

    hasPermission(perm: string): boolean {
      return this.permissions.includes(perm)
    },
  },
})
