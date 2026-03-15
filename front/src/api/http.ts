import { getAuthToken } from '@/api/auth'

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface RequestOptions extends RequestInit {
  withAuth?: boolean
  parseErrorMessage?: boolean
  unauthorizedMessage?: string | null
}

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

export async function request<T>(path: string, options?: RequestOptions): Promise<T> {
  const withAuth = options?.withAuth ?? true
  const parseErrorMessage = options?.parseErrorMessage ?? true
  const unauthorizedMessage = options?.unauthorizedMessage ?? '登录已过期，请重新登录'

  const headers = new Headers(options?.headers)
  if (!headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  const token = withAuth ? getAuthToken() : null
  if (token && !headers.has('Authorization')) {
    headers.set('Authorization', `Bearer ${token}`)
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
  })

  if (!response.ok) {
    if (response.status === 401 && unauthorizedMessage) {
      throw new Error(unauthorizedMessage)
    }

    if (parseErrorMessage) {
      try {
        const errorBody = (await response.json()) as ApiResponse<null>
        if (errorBody.message) {
          throw new Error(errorBody.message)
        }
      } catch {
      }
    }

    throw new Error(`HTTP 请求失败：${response.status}`)
  }

  const body = (await response.json()) as ApiResponse<T>
  if (body.code !== 0) {
    throw new Error(body.message || '业务请求失败')
  }
  return body.data
}
