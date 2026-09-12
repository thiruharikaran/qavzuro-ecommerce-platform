import React, { createContext, useContext, useEffect, useState, useCallback } from 'react'
import { authApi } from '../api/authApi'
import { userApi } from '../api/userApi'
import { setAuthTokens, loadStoredTokens, registerAuthChangeHandler } from '../api/client'
import { decodeJwtPayload } from '../utils/jwt'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [permissions, setPermissions] = useState(new Set())
  const [loading, setLoading] = useState(true)

  const applyAuthResult = useCallback((data) => {
    if (!data) {
      setUser(null)
      setPermissions(new Set())
      return
    }
    setAuthTokens(data)
    setUser(data.user)
    setPermissions(new Set(data.permissions || []))
  }, [])

  useEffect(() => {
    registerAuthChangeHandler((data) => {
      if (!data) {
        setUser(null)
        setPermissions(new Set())
      } else if (data.user) {
        applyAuthResult(data)
      }
    })

    const stored = loadStoredTokens()
    if (stored.accessToken) {
      const claims = decodeJwtPayload(stored.accessToken)
      const perms = claims?.permissions ? claims.permissions.split(',').filter(Boolean) : []
      setPermissions(new Set(perms))
      userApi.me()
        .then((me) => setUser(me))
        .catch(() => { setAuthTokens(null) })
        .finally(() => setLoading(false))
    } else {
      setLoading(false)
    }
  }, [applyAuthResult])

  const login = useCallback(async (email, password) => {
    const data = await authApi.login({ email, password })
    applyAuthResult(data)
    return data
  }, [applyAuthResult])

  const register = useCallback(async (payload) => {
    const data = await authApi.register(payload)
    applyAuthResult(data)
    return data
  }, [applyAuthResult])

  const logout = useCallback(async () => {
    const stored = loadStoredTokens()
    try { await authApi.logout(stored.refreshToken) } catch { /* best-effort */ }
    setAuthTokens(null)
    setUser(null)
    setPermissions(new Set())
  }, [])

  const hasPermission = useCallback((code) => permissions.has(code), [permissions])
  const hasRole = useCallback((code) => (user?.roleCodes || []).includes(code), [user])

  const value = { user, setUser, permissions, hasPermission, hasRole, loading, login, register, logout }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
