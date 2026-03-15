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

function stringifyErrorDetail(value: unknown): string {
  if (value == null) return ''
  if (typeof value === 'string') return value
  if (Array.isArray(value)) {
    return value
      .map((item) => stringifyErrorDetail(item))
      .filter((item) => item.length > 0)
      .join('\n')
  }
  if (typeof value === 'object') {
    const lines = Object.entries(value as Record<string, unknown>)
      .map(([key, detail]) => {
        const text = stringifyErrorDetail(detail)
        return text ? `${key}: ${text}` : ''
      })
      .filter((line) => line.length > 0)
    return lines.join('\n')
  }
  return String(value)
}

async function parseResponseErrorMessage(response: Response): Promise<string> {
  const backup = response.clone()
  try {
    const errorBody = (await response.json()) as Partial<ApiResponse<unknown>> & Record<string, unknown>
    const message = typeof errorBody.message === 'string' ? errorBody.message.trim() : ''
    const code = typeof errorBody.code === 'number' ? errorBody.code : null
    const detail = stringifyErrorDetail(
      errorBody.data ?? errorBody.detail ?? errorBody.details ?? errorBody.error
    ).trim()

    if (message && code !== null) {
      return detail ? `[${code}] ${message}\n${detail}` : `[${code}] ${message}`
    }
    if (message) {
      return detail ? `${message}\n${detail}` : message
    }
  } catch {
  }

  try {
    const raw = (await backup.text()).trim()
    if (raw) return raw
  } catch {
  }
  return ''
}

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
      const parsedErrorMessage = await parseResponseErrorMessage(response)
      if (parsedErrorMessage) {
        throw new Error(`HTTP ${response.status}: ${parsedErrorMessage}`)
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
