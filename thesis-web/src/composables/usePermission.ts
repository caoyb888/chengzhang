import { usePermissionStore } from '@/stores/permission'

export function usePermission() {
  const permissionStore = usePermissionStore()

  function checkPermission(perm: string): boolean {
    return permissionStore.hasPermission(perm)
  }

  function checkAnyPermission(perms: string[]): boolean {
    return permissionStore.hasAnyPermission(perms)
  }

  return {
    checkPermission,
    checkAnyPermission,
  }
}
