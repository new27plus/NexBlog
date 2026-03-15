import { request } from '@/api/http'

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
