/*
 * 这个文件的定位：前端“文章模块”的 API 层。
 *
 * 为什么要单独建 API 层：
 * 1) 让页面组件只负责展示，不直接拼 fetch 细节。
 * 2) 后续如果你把 fetch 换成 axios，只改这一层即可。
 * 3) 接口地址、返回结构、错误处理都能统一管理。
 */

export interface ApiResponse<T> {
  /*
   * code:
   * - 0 表示成功
   * - 非 0 表示业务失败（例如参数错误、资源不存在）
   */
  code: number
  /*
   * message:
   * - 给用户看的提示信息
   * - 失败时通常需要展示这段文案
   */
  message: string
  /*
   * data:
   * - 接口真实数据
   * - 类型通过泛型 T 指定
   */
  data: T
}

export interface PageData<T> {
  content: T[]
  totalPages: number
  totalElements: number
  size: number
  number: number
}

export interface ArticleListItem {
  id: number
  title: string
  summary: string | null
  createdAt: string
}

export interface ArticleDetail {
  id: number
  title: string
  content: string
  summary: string | null
  createdAt: string
  updatedAt: string
}

export interface ArticleWritePayload {
  title: string
  content: string
  summary?: string
}

/*
 * 环境变量说明：
 * - 本地开发：VITE_API_BASE_URL=http://localhost:8080
 * - 未配置时用默认值，方便你开箱即跑
 */
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(init?.headers ?? {}),
    },
    ...init,
  })

  /*
   * 为什么先判断 HTTP 状态码：
   * - 4xx/5xx 时后端可能都还没返回业务结构，先兜底更稳
   */
  if (!response.ok) {
    throw new Error(`HTTP 请求失败：${response.status}`)
  }

  const body = (await response.json()) as ApiResponse<T>

  /*
   * 为什么判断业务 code：
   * - HTTP 成功不代表业务成功
   * - 例如参数校验失败，可能是 200 + code 非 0（视后端设计）
   */
  if (body.code !== 0) {
    throw new Error(body.message || '业务请求失败')
  }

  return body.data
}

export async function getPublicArticles(params?: {
  page?: number
  size?: number
  keyword?: string
}): Promise<PageData<ArticleListItem>> {
  const search = new URLSearchParams()
  if (params?.page !== undefined) search.set('page', String(params.page))
  if (params?.size !== undefined) search.set('size', String(params.size))
  if (params?.keyword) search.set('keyword', params.keyword)
  const queryString = search.toString() ? `?${search}` : ''
  return request<PageData<ArticleListItem>>(`/api/public/articles${queryString}`)
}

export async function getPublicArticleDetail(id: number): Promise<ArticleDetail> {
  return request<ArticleDetail>(`/api/public/articles/${id}`)
}

export async function getStudioArticles(params?: {
  page?: number
  size?: number
  keyword?: string
}): Promise<PageData<ArticleListItem>> {
  const search = new URLSearchParams()
  if (params?.page !== undefined) search.set('page', String(params.page))
  if (params?.size !== undefined) search.set('size', String(params.size))
  if (params?.keyword) search.set('keyword', params.keyword)
  const queryString = search.toString() ? `?${search}` : ''
  return request<PageData<ArticleListItem>>(`/api/studio/articles${queryString}`)
}

export async function getStudioArticleDetail(id: number): Promise<ArticleDetail> {
  return request<ArticleDetail>(`/api/studio/articles/${id}`)
}

export async function createStudioArticle(payload: ArticleWritePayload): Promise<ArticleDetail> {
  return request<ArticleDetail>('/api/studio/articles', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export async function updateStudioArticle(id: number, payload: ArticleWritePayload): Promise<ArticleDetail> {
  return request<ArticleDetail>(`/api/studio/articles/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export async function deleteStudioArticle(id: number): Promise<void> {
  await request<void>(`/api/studio/articles/${id}`, {
    method: 'DELETE',
  })
}
