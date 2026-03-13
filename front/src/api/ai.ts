import { getAuthToken } from '@/api/auth'

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export type AiOptimizeMode = 'replace' | 'compare'

export interface AiCompareResult {
  original: string
  optimized: string
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const token = getAuthToken()
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(init?.headers ?? {}),
    },
    ...init,
  })

  if (!response.ok) {
    if (response.status === 401) {
      throw new Error('登录已过期，请重新登录')
    }
    throw new Error(`HTTP 请求失败：${response.status}`)
  }

  const body = (await response.json()) as ApiResponse<T>
  if (body.code !== 0) {
    throw new Error(body.message || '业务请求失败')
  }

  return body.data
}

export async function optimizeAiContent(
  content: string,
  mode: AiOptimizeMode = 'replace',
): Promise<string | AiCompareResult> {
  return request<string | AiCompareResult>('/api/ai/optimize', {
    method: 'POST',
    body: JSON.stringify({ content, mode }),
  })
}

export async function summarizeAiContent(content: string): Promise<string> {
  return request<string>('/api/ai/summary', {
    method: 'POST',
    body: JSON.stringify({ content }),
  })
}
