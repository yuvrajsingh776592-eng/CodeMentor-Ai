import { createContext, useContext, useEffect, useState } from 'react'
import type { ReactNode } from 'react'
import { api, clearSession, hasSession, saveSession } from './api'
import type { User } from './types'
type AuthValue = { user: User | null; loading: boolean; login: (email: string, password: string) => Promise<void>; register: (name: string, email: string, password: string) => Promise<void>; logout: () => void }
const AuthContext = createContext<AuthValue | null>(null)
export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null); const [loading, setLoading] = useState(true)
  useEffect(() => { if (!hasSession()) { setLoading(false); return } api<User>('/api/auth/me').then(setUser).catch(clearSession).finally(() => setLoading(false)) }, [])
  async function authenticate(path: string, body: object) { const response = await api<{ token: string; user: User }>(path, { method: 'POST', body: JSON.stringify(body) }); saveSession(response.token); setUser(response.user) }
  return <AuthContext.Provider value={{ user, loading, login: (email, password) => authenticate('/api/auth/login', { email, password }), register: (name, email, password) => authenticate('/api/auth/register', { name, email, password }), logout: () => { clearSession(); setUser(null) } }}>{children}</AuthContext.Provider>
}
export function useAuth() { const value = useContext(AuthContext); if (!value) throw new Error('AuthProvider is missing'); return value }
