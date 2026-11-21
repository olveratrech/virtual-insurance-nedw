import { useEffect, useMemo, useState } from 'react'
import {
  getCurrentSession,
  login as loginRequest,
  logout as logoutRequest,
  register as registerRequest,
} from '../services/authApi.js'
import AuthContext from './authContext.js'

export function AuthProvider({ children }) {
  const [client, setClient] = useState(null)
  const [sessionStatus, setSessionStatus] = useState('checking')

  async function refreshSession() {
    setSessionStatus('checking')

    try {
      const session = await getCurrentSession()
      setClient(session?.client ?? null)
      setSessionStatus(session ? 'authenticated' : 'anonymous')
    } catch {
      setClient(null)
      setSessionStatus('error')
    }
  }

  useEffect(() => {
    let active = true

    getCurrentSession()
      .then((session) => {
        if (!active) return
        setClient(session?.client ?? null)
        setSessionStatus(session ? 'authenticated' : 'anonymous')
      })
      .catch(() => {
        if (!active) return
        setClient(null)
        setSessionStatus('error')
      })

    return () => {
      active = false
    }
  }, [])

  async function login(credentials) {
    const session = await loginRequest(credentials)
    setClient(session.client)
    setSessionStatus('authenticated')
    return session.client
  }

  async function logout() {
    await logoutRequest()
    setClient(null)
    setSessionStatus('anonymous')
  }

  async function register(data, photo) {
    const client = await registerRequest(data, photo)
    setClient(client)
    setSessionStatus('authenticated')
    return client
  }

  const value = useMemo(() => ({
    client,
    sessionStatus,
    login,
    logout,
    register,
    refreshSession,
  }), [client, sessionStatus])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

