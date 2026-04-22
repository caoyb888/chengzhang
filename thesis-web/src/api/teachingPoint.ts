import request from '@/utils/request'
import type { TeachingPointVO, PageResult, PageParams } from '@/types'

export interface CreateTeachingPointData {
  name: string
  code?: string
  contactName?: string
  contactPhone?: string
  dataScope?: string
}

export interface UpdateTeachingPointData {
  name?: string
  code?: string
  contactName?: string
  contactPhone?: string
  dataScope?: string
  status?: string
}

export interface TeachingPointQueryParams extends PageParams {
  keyword?: string
  status?: string
}

export const teachingPointApi = {
  create: (data: CreateTeachingPointData) =>
    request.post<{ id: string }>('/teaching-point', data),

  update: (id: string, data: UpdateTeachingPointData) =>
    request.put<void>(`/teaching-point/${id}`, data),

  delete: (id: string) =>
    request.delete<void>(`/teaching-point/${id}`),

  getDetail: (id: string) =>
    request.get<TeachingPointVO>(`/teaching-point/${id}`),

  pageList: (params: TeachingPointQueryParams) =>
    request.get<PageResult<TeachingPointVO>>('/teaching-point/list', { params }),

  listAll: () =>
    request.get<TeachingPointVO[]>('/teaching-point/all'),
}
