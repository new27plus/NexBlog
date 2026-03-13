import { getAuthToken } from '@/api/auth'

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface SystemConfigDetail {
  id: number | null
  githubOwner: string | null
  githubRepo: string | null
  publishBranch: string | null
  tokenConfigured: boolean
  siteBasePath: string | null
}

export interface SystemConfigWritePayload {
  githubOwner: string
  githubRepo: string
  publishBranch: string
  githubToken?: string
  siteBasePath: string
}

export interface PublishPrepareResponse {
  jobId: string
  articleCount: number
  distPath: string
  previewPath: string
}

export interface PublishReleasePayload {
  jobId: string
}

export interface PublishReleaseResponse {
  jobId: string
  branch: string
  commitId: string
  publishedUrl: string
}

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

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
    try {
      const errorBody = (await response.json()) as ApiResponse<null>
      if (errorBody.message) {
        throw new Error(errorBody.message)
      }
    } catch {
    }
    throw new Error(`HTTP 请求失败：${response.status}`)
  }

  const body = (await response.json()) as ApiResponse<T>
  if (body.code !== 0) {
    throw new Error(body.message || '业务请求失败')
  }
  return body.data
}

export async function getSystemConfig(): Promise<SystemConfigDetail> {
  return request<SystemConfigDetail>('/api/studio/system-config')
}

export async function upsertSystemConfig(payload: SystemConfigWritePayload): Promise<SystemConfigDetail> {
  return request<SystemConfigDetail>('/api/studio/system-config', {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export async function preparePublish(): Promise<PublishPrepareResponse> {
  return request<PublishPrepareResponse>('/api/studio/publish/prepare', {
    method: 'POST',
  })
}

export async function releasePublish(payload: PublishReleasePayload): Promise<PublishReleaseResponse> {
  return request<PublishReleaseResponse>('/api/studio/publish/release', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}
