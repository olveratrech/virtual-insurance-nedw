import { useEffect, useMemo, useState } from 'react'
import {
  getCurrentAdminSession,
  loginAdmin as loginRequest,
  logoutAdmin as logoutRequest,
} from '../services/adminAuthApi.js'
import AdminAuthContext from './adminAuthContext.js'

export function AdminAuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [sessionStatus, setSessionStatus] = useState('checking')

  async function refreshSession() {
    setSessionStatus('checking')
    try {
      const session = await getCurrentAdminSession()
      setUser(session?.user ?? null)
      setSessionStatus(session ? 'authenticated' : 'anonymous')
    } catch {
      setUser(null)
      setSessionStatus('error')
    }
  }

  useEffect(() => {
    let active = true
    getCurrentAdminSession()
      .then((session) => {
        if (!active) return
        setUser(session?.user ?? null)
        setSessionStatus(session ? 'authenticated' : 'anonymous')
      })
      .catch(() => {
        if (!active) return
        setUser(null)
        setSessionStatus('error')
      })
    return () => { active = false }
  }, [])

  async function login(credentials) {
    const session = await loginRequest(credentials)
    setUser(session.user)
    setSessionStatus('authenticated')
    return session.user
  }

  async function logout() {
    await logoutRequest()
    setUser(null)
    setSessionStatus('anonymous')
  }

  const value = useMemo(() => ({
    user,
    sessionStatus,
    login,
    logout,
    refreshSession,
  }), [user, sessionStatus])

  return <AdminAuthContext.Provider value={value}>{children}</AdminAuthContext.Provider>
}
