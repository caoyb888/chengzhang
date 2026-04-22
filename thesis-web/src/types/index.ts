export * from './enums'

export interface Result<T = unknown> {
  code: number
  message: string
  data: T
  timestamp: number
}

export interface PageResult<T> {
  total: number
  pages: number
  current: number
  size: number
  records: T[]
}

export interface PageParams {
  pageNum?: number
  pageSize?: number
}

export interface LoginUser {
  userId: string
  username: string
  realName: string
  userType: import('./enums').UserType
  schoolId: string
  teachingPointId?: string
  avatarUrl?: string
  permissions: string[]
}

export interface LoginVO {
  accessToken: string
  refreshToken: string
  expiresIn: number
  userInfo: LoginUser
}

export interface UserVO {
  userId: string
  username: string
  realName: string
  phone: string
  userType: import('./enums').UserType
  studentNo?: string
  teacherNo?: string
  major?: string
  department?: string
  teachingPointId?: string
  teachingPointName?: string
  status: string
  lastLoginAt?: string
  createdAt: string
}

export interface TeachingPointVO {
  id: string
  schoolId: string
  name: string
  code?: string
  contactName?: string
  contactPhone?: string
  dataScope: string
  status: string
  sortOrder: number
  createdAt: string
}

export interface RoleVO {
  roleId: string
  roleCode: string
  roleName: string
  isPreset: boolean
  description?: string
  permCount: number
  isActive: boolean
}

export interface PermissionVO {
  permId: string
  permCode: string
  permName: string
  permType: string
  module: string
  parentId?: string
  routePath?: string
  icon?: string
  sortOrder: number
  children?: PermissionVO[]
}

export interface ImportProgressVO {
  taskId: string
  status: string
  totalCount: number
  successCount: number
  failCount: number
  progress: number
  errorFileUrl?: string
  startedAt?: string
  finishedAt?: string
}
