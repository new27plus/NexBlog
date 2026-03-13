export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface LoginResponse {
  username: string
  accessToken: string
  expiresAt: string
}

export interface BootstrapStatusResponse {
  bootstrapRequired: boolean
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'
const AUTH_TOKEN_KEY = 'nexblog.studio.token'
const AUTH_EXPIRES_AT_KEY = 'nexblog.studio.tokenExpiresAt'
const AUTH_USERNAME_KEY = 'nexblog.studio.username'

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(init?.headers ?? {}),
    },
    ...init,
  })

  const body = (await response.json()) as ApiResponse<T>
  if (!response.ok) {
    throw new Error(body.message || `HTTP 请求失败：${response.status}`)
  }
  if (body.code !== 0) {
    throw new Error(body.message || '业务请求失败')
  }
  return body.data
}

export async function fetchBootstrapStatus(): Promise<BootstrapStatusResponse> {
  return request<BootstrapStatusResponse>('/api/auth/bootstrap-status')
}

export async function bootstrapStudio(
  username: string,
  password: string,
  confirmPassword: string,
): Promise<LoginResponse> {
  return request<LoginResponse>('/api/auth/bootstrap', {
    method: 'POST',
    body: JSON.stringify({ username, password, confirmPassword }),
  })
}

export async function loginStudio(username: string, password: string): Promise<LoginResponse> {
  return request<LoginResponse>('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  })
}

export function saveAuthSession(session: LoginResponse): void {
  localStorage.setItem(AUTH_TOKEN_KEY, session.accessToken)
  localStorage.setItem(AUTH_EXPIRES_AT_KEY, session.expiresAt)
  localStorage.setItem(AUTH_USERNAME_KEY, session.username)
}

export function clearAuthSession(): void {
  localStorage.removeItem(AUTH_TOKEN_KEY)
  localStorage.removeItem(AUTH_EXPIRES_AT_KEY)
  localStorage.removeItem(AUTH_USERNAME_KEY)
  localStorage.removeItem('nexblog.studio.loggedIn')
}

export function getAuthToken(): string | null {
  return localStorage.getItem(AUTH_TOKEN_KEY)
}

export function getAuthUsername(): string {
  return localStorage.getItem(AUTH_USERNAME_KEY) ?? 'Admin'
}

export function isAuthenticated(): boolean {
  const token = localStorage.getItem(AUTH_TOKEN_KEY)
  const expiresAt = localStorage.getItem(AUTH_EXPIRES_AT_KEY)
  if (!token || !expiresAt) {
    return false
  }
  const expiresAtMs = new Date(expiresAt).getTime()
  if (Number.isNaN(expiresAtMs) || Date.now() >= expiresAtMs) {
    clearAuthSession()
    return false
  }
  return true
}
