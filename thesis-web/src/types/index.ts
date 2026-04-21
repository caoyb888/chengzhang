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
  userId: number
  username: string
  realName: string
  userType: import('./enums').UserType
  schoolId: number
  teachingPointId?: number
  avatarUrl?: string
  permissions: string[]
  roles: string[]
}
