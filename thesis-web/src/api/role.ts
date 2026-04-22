import request from '@/utils/request'
import type { RoleVO, PermissionVO, PageResult } from '@/types'

export interface CreateRoleData {
  roleCode: string
  roleName: string
  description?: string
  permIds: number[]
}

export interface UpdateRolePermissionsData {
  permIds: number[]
}

export interface AssignUserRoleData {
  roleIds: number[]
}

export const roleApi = {
  listRoles: () =>
    request.get<RoleVO[]>('/role/list'),

  createRole: (data: CreateRoleData) =>
    request.post<{ roleId: string }>('/role', data),

  updateRole: (roleId: string, data: Partial<CreateRoleData>) =>
    request.put<void>(`/role/${roleId}`, data),

  deleteRole: (roleId: string) =>
    request.delete<void>(`/role/${roleId}`),

  updateRolePermissions: (roleId: string, data: UpdateRolePermissionsData) =>
    request.put<void>(`/role/${roleId}/permissions`, data),

  applyPresetRole: (presetRoleCode: string, targetRoleId?: string) =>
    request.post<void>(`/role/preset/${presetRoleCode}/apply`, { targetRoleId }),

  getPermissionTree: () =>
    request.get<PermissionVO[]>('/role/permissions/tree'),

  assignUserRoles: (userId: string, data: AssignUserRoleData) =>
    request.post<void>(`/user/${userId}/roles`, data),
}
