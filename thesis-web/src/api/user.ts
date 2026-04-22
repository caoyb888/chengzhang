import request from '@/utils/request'
import type { UserVO, PageResult, PageParams, ImportProgressVO } from '@/types'

export interface CreateUserData {
  userType: string
  realName: string
  username: string
  phone: string
  password?: string
  studentNo?: string
  teacherNo?: string
  major?: string
  department?: string
  teachingPointId?: string | number
  batchId?: string | number
}

export interface UpdateUserData {
  username?: string
  phone?: string
  password?: string
  major?: string
  teachingPointId?: string | number
  status?: string
  realName?: string
}

export interface UserQueryParams extends PageParams {
  userType?: string
  teachingPointId?: string
  keyword?: string
  major?: string
  status?: string
}

export interface ResetPasswordData {
  newPassword: string
}

export const userApi = {
  createUser: (data: CreateUserData) =>
    request.post<{ userId: string }>('/user', data),

  updateUser: (userId: string, data: UpdateUserData) =>
    request.put<void>(`/user/${userId}`, data),

  deleteUser: (userId: string) =>
    request.delete<void>(`/user/${userId}`),

  getUserDetail: (userId: string) =>
    request.get<UserVO>(`/user/${userId}`),

  getCurrentUser: () =>
    request.get<UserVO>('/user/me'),

  pageUsers: (params: UserQueryParams) =>
    request.get<PageResult<UserVO>>('/user/list', { params }),

  resetPassword: (userId: string, data: ResetPasswordData) =>
    request.put<void>(`/user/${userId}/reset-password`, data),

  updateStatus: (userId: string, status: string) =>
    request.put<void>(`/user/${userId}/status`, { status }),

  importUsers: (formData: FormData) =>
    request.post<{ taskId: string; totalCount: number | null; description: string }>('/user/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    }),

  getImportProgress: (taskId: string) =>
    request.get<ImportProgressVO>(`/user/import/${taskId}/progress`),
}
