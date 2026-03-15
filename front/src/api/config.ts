import { request } from '@/api/http'
export { API_BASE_URL } from '@/api/http'

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

export async function getLatestPublish(): Promise<PublishPrepareResponse | null> {
  return request<PublishPrepareResponse | null>('/api/studio/publish/latest')
}

export async function releasePublish(payload: PublishReleasePayload): Promise<PublishReleaseResponse> {
  return request<PublishReleaseResponse>('/api/studio/publish/release', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}
