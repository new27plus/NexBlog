import { request } from '@/api/http'

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface PageData<T> {
  content: T[]
  totalPages: number
  totalElements: number
  size: number
  number: number
}

export interface CategoryListItem {
  id: number
  name: string
  createdAt: string
  updatedAt: string
}

export interface CategoryDetail {
  id: number
  name: string
  createdAt: string
  updatedAt: string
}

export interface CategoryWritePayload {
  name: string
}

export async function getStudioCategories(params?: {
  page?: number
  size?: number
  keyword?: string
}): Promise<PageData<CategoryListItem>> {
  const search = new URLSearchParams()
  if (params?.page !== undefined) search.set('page', String(params.page))
  if (params?.size !== undefined) search.set('size', String(params.size))
  if (params?.keyword) search.set('keyword', params.keyword)
  const queryString = search.toString() ? `?${search}` : ''
  return request<PageData<CategoryListItem>>(`/api/studio/categories${queryString}`)
}

export async function getStudioCategoryDetail(id: number): Promise<CategoryDetail> {
  return request<CategoryDetail>(`/api/studio/categories/${id}`)
}

export async function createStudioCategory(payload: CategoryWritePayload): Promise<CategoryDetail> {
  return request<CategoryDetail>('/api/studio/categories', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export async function updateStudioCategory(id: number, payload: CategoryWritePayload): Promise<CategoryDetail> {
  return request<CategoryDetail>(`/api/studio/categories/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export async function deleteStudioCategory(id: number): Promise<void> {
  await request<void>(`/api/studio/categories/${id}`, {
    method: 'DELETE',
  })
}

