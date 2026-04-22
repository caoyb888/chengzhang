import request from '@/utils/request'
import type { LoginVO, Result } from '@/types'

export interface LoginByPasswordData {
  username: string
  password: string
  clientType?: string
}

export interface LoginBySmsData {
  phone: string
  smsCode: string
  clientType?: string
}

export interface SmsCodeSendData {
  phone: string
  purpose?: string
}

export const authApi = {
  loginByPassword: (data: LoginByPasswordData) =>
    request.post<LoginVO>('/auth/login/password', data),

  loginBySms: (data: LoginBySmsData) =>
    request.post<LoginVO>('/auth/login/sms', data),

  sendSmsCode: (data: SmsCodeSendData) =>
    request.post<{ expireSeconds: number }>('/auth/sms/send', data),

  refreshToken: (refreshToken: string) =>
    request.post<{ accessToken: string; expiresIn: number }>('/auth/token/refresh', { refreshToken }),

  logout: () => request.post<void>('/auth/logout'),
}
