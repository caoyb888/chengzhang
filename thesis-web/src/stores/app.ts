import { defineStore } from 'pinia'
import type { AppTheme } from '@/types'
import { UserType, ROLE_DEFAULT_THEME } from '@/types'

interface AppState {
  theme: AppTheme
  sidebarCollapsed: boolean
  locale: string
}

export const useAppStore = defineStore('app', {
  state: (): AppState => ({
    theme: (localStorage.getItem('thesis_theme') as AppTheme) ?? 'scholar',
    sidebarCollapsed: false,
    locale: 'zh-CN',
  }),

  actions: {
    setTheme(theme: AppTheme) {
      this.theme = theme
      document.documentElement.setAttribute('data-theme', theme)
      localStorage.setItem('thesis_theme', theme)
    },

    applyRoleDefaultTheme(userType: UserType) {
      const defaultTheme = ROLE_DEFAULT_THEME[userType] ?? 'scholar'
      const saved = localStorage.getItem('thesis_theme') as AppTheme | null
      this.setTheme(saved ?? defaultTheme)
    },

    toggleSidebar() {
      this.sidebarCollapsed = !this.sidebarCollapsed
    },
  },
})
