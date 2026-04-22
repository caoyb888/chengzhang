import { defineStore } from 'pinia'

interface PermissionState {
  permissions: string[]
  routesAdded: boolean
}

export const usePermissionStore = defineStore('permission', {
  state: (): PermissionState => ({
    permissions: [],
    routesAdded: false,
  }),

  getters: {
    hasPermission: (state) => (perm: string): boolean =>
      state.permissions.includes(perm),

    hasAllPermissions: (state) => (perms: string[]): boolean =>
      perms.every((p) => state.permissions.includes(p)),

    hasAnyPermission: (state) => (perms: string[]): boolean =>
      perms.some((p) => state.permissions.includes(p)),
  },

  actions: {
    initPermissions(permissions: string[]) {
      this.permissions = permissions
    },

    markRoutesAdded() {
      this.routesAdded = true
    },

    reset() {
      this.$reset()
    },
  },
})
