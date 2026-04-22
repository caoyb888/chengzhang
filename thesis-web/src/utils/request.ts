import axios, { type AxiosRequestConfig, type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import type { Result } from '@/types'

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api/v1'

const request = axios.create({
  baseURL: BASE_URL,
  timeout: 15000,
})

let isRefreshing = false
let pendingQueue: Array<(token: string) => void> = []

function getAccessToken(): string | null {
  return localStorage.getItem('thesis_access_token')
}

function getRefreshToken(): string | null {
  return localStorage.getItem('thesis_refresh_token')
}

function setTokens(accessToken: string, refreshToken: string): void {
  localStorage.setItem('thesis_access_token', accessToken)
  localStorage.setItem('thesis_refresh_token', refreshToken)
}

export function clearAuth(): void {
  localStorage.removeItem('thesis_access_token')
  localStorage.removeItem('thesis_refresh_token')
  localStorage.removeItem('thesis_user')
}

request.interceptors.request.use((config) => {
  const token = getAccessToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response: AxiosResponse<Result<unknown>>) => {
    const { code, message, data } = response.data
    if (code === 200) {
      return data as unknown as AxiosResponse
    }
    if (code === 401 || code === 4010) {
      handleUnauthorized()
      return Promise.reject(new Error(message))
    }
    ElMessage.error(message || '请求失败')
    return Promise.reject(new Error(message))
  },
  async (error) => {
    const { response, config } = error
    const resData = response?.data as Result<unknown> | undefined
    const code = resData?.code

    if ((code === 401 || code === 4010) && config && !config._retry) {
      if (isRefreshing) {
        return new Promise((resolve) => {
          pendingQueue.push((token) => {
            config.headers.Authorization = `Bearer ${token}`
            resolve(request(config))
          })
        })
      }
      config._retry = true
      isRefreshing = true
      try {
        const refreshToken = getRefreshToken()
        if (!refreshToken) throw new Error('no refresh token')
        const newToken = await refreshAccessToken(refreshToken)
        setTokens(newToken, refreshToken)
        pendingQueue.forEach((cb) => cb(newToken))
        pendingQueue = []
        config.headers.Authorization = `Bearer ${newToken}`
        return request(config)
      } catch {
        clearAuth()
        window.location.href = '/login'
        return Promise.reject(error)
      } finally {
        isRefreshing = false
      }
    }
    ElMessage.error(resData?.message || '网络异常，请稍后重试')
    return Promise.reject(error)
  },
)

async function refreshAccessToken(refreshToken: string): Promise<string> {
  const res = await axios.post<Result<{ accessToken: string; expiresIn: number }>>(
    `${BASE_URL}/auth/token/refresh`,
    { refreshToken },
  )
  return res.data.data.accessToken
}

function handleUnauthorized(): void {
  clearAuth()
  ElMessage.error('登录已过期，请重新登录')
  setTimeout(() => {
    window.location.href = '/login'
  }, 1500)
}

export default {
  get<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return request.get(url, config)
  },
  post<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
    return request.post(url, data, config)
  },
  put<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
    return request.put(url, data, config)
  },
  delete<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return request.delete(url, config)
  },
}
