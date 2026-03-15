import { request } from '@/api/http'

// 统一响应外壳：
// 后端通常返回 { code, message, data }，这里用泛型 T 指定 data 的真实类型
export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

// 页面分区键：
// 这 4 个字符串会作为 sections 的固定键名，写错键名会被 TypeScript 直接拦截
export type SectionKey = 'intro' | 'links' | 'topics' | 'updates'

// 单条个人内容项：
// 每个分区里是 PersonalItem[]，即“一个分区可以有多条内容”
export interface PersonalItem {
  id: string
  title: string
  subtitle: string
  content: string
  tag: string
  linkLabel: string
  linkUrl: string
}

// 个人配置详情：
// sections 使用 Record<SectionKey, PersonalItem[]>，确保 4 个分区结构完整且类型安全
export interface PersonalConfig {
  id: number | null
  sections: Record<SectionKey, PersonalItem[]>
  createdAt?: string | null
  updatedAt?: string | null
}

// 写入接口：
// 提交时只传可编辑业务字段，避免把只读字段（如 createdAt）一起提交
export interface PersonalConfigWritePayload {
  sections: Record<SectionKey, PersonalItem[]>
}

// 后台读取个人配置：
// Promise<PersonalConfig> 表示“异步完成后会拿到 PersonalConfig”
export async function getStudioPersonalConfig(): Promise<PersonalConfig> {
  return request<PersonalConfig>('/api/studio/personal-config')
}

// 后台保存个人配置（语义上是 upsert）：
// 由后端决定是新增还是更新
export async function updatePersonalConfig(payload: PersonalConfigWritePayload): Promise<PersonalConfig> {
  return request<PersonalConfig>('/api/studio/personal-config', {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

// 前台公开读取个人配置：
// 如果你的后端路由是 /api/public/personal-config，这里改成对应路径即可
export async function getPersonalConfig(): Promise<PersonalConfig> {
  return request<PersonalConfig>('/api/personal-config')
}
