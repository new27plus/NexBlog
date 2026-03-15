/*
 * 这个文件的定位：前端“文章模块”的 API 层。
 *
 * 为什么要单独建 API 层：
 * 1) 让页面组件只负责展示，不直接拼 fetch 细节。
 * 2) 后续如果你把 fetch 换成 axios，只改这一层即可。
 * 3) 接口地址、返回结构、错误处理都能统一管理。
 */

import { request } from '@/api/http'

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
  categoryId: number | null
  categoryName: string | null
  createdAt: string
}

export interface ArticleDetail {
  id: number
  title: string
  content: string
  summary: string | null
  categoryId: number | null
  categoryName: string | null
  createdAt: string
  updatedAt: string
}

export interface ArticleWritePayload {
  title: string
  content: string
  summary?: string
  categoryId?: number | null
}

const IS_STATIC_EXPORT = import.meta.env.VITE_STATIC_EXPORT === 'true'
const STATIC_SITE_BASE_PATH = normalizeBasePath(import.meta.env.VITE_SITE_BASE_PATH ?? '/')

function normalizeBasePath(basePath: string): string {
  let normalized = basePath.trim()
  if (!normalized.startsWith('/')) {
    normalized = `/${normalized}`
  }
  if (!normalized.endsWith('/')) {
    normalized = `${normalized}/`
  }
  return normalized
}

function buildStaticUrl(relativePath: string): string {
  const cleanPath = relativePath.startsWith('/') ? relativePath.slice(1) : relativePath
  return `${STATIC_SITE_BASE_PATH}${cleanPath}`
}

export async function getPublicArticles(params?: {
  page?: number
  size?: number
  keyword?: string
}): Promise<PageData<ArticleListItem>> {
  if (IS_STATIC_EXPORT) {
    const response = await fetch(buildStaticUrl('/export/articles.json'))
    if (!response.ok) {
      throw new Error(`静态文章列表读取失败：${response.status}`)
    }
    const list = (await response.json()) as ArticleListItem[]
    const keyword = (params?.keyword ?? '').trim().toLowerCase()
    const filtered = keyword
      ? list.filter(item =>
          item.title.toLowerCase().includes(keyword)
          || (item.summary ?? '').toLowerCase().includes(keyword)
        )
      : list

    const page = params?.page ?? 0
    const size = params?.size ?? 10
    const start = page * size
    const end = start + size
    const content = filtered.slice(start, end)
    const totalElements = filtered.length
    const totalPages = totalElements === 0 ? 0 : Math.ceil(totalElements / size)

    return {
      content,
      totalPages,
      totalElements,
      size,
      number: page,
    }
  }

  const search = new URLSearchParams()
  if (params?.page !== undefined) search.set('page', String(params.page))
  if (params?.size !== undefined) search.set('size', String(params.size))
  if (params?.keyword) search.set('keyword', params.keyword)
  const queryString = search.toString() ? `?${search}` : ''
  return request<PageData<ArticleListItem>>(`/api/public/articles${queryString}`)
}

export async function getPublicArticleDetail(id: number): Promise<ArticleDetail> {
  if (IS_STATIC_EXPORT) {
    const response = await fetch(buildStaticUrl(`/export/articles/${id}.json`))
    if (!response.ok) {
      throw new Error(`静态文章详情读取失败：${response.status}`)
    }
    return (await response.json()) as ArticleDetail
  }
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
