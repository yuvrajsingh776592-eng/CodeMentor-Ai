const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'
export class ApiError extends Error { constructor(message: string, public status: number) { super(message) } }
export async function api<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = sessionStorage.getItem('cm_token')
  const response = await fetch(`${API_URL}${path}`, { ...options, headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}), ...options.headers } })
  if (!response.ok) { const error = await response.json().catch(() => null); throw new ApiError(error?.message || error?.detail || 'Request failed. Please try again.', response.status) }
  if (response.status === 204) return undefined as T
  return response.json() as Promise<T>
}
export function saveSession(token: string) { sessionStorage.setItem('cm_token', token) }
export function clearSession() { sessionStorage.removeItem('cm_token') }
export function hasSession() { return Boolean(sessionStorage.getItem('cm_token')) }
