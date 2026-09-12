import React from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

/** Requires authentication, and optionally a specific permission. This is a UX
 * convenience only — every protected operation is also enforced by the backend. */
export default function ProtectedRoute({ children, requirePermission }) {
  const { user, loading, hasPermission } = useAuth()
  const location = useLocation()

  if (loading) return <div className="p-10 text-center text-ink-400">Loading…</div>
  if (!user) return <Navigate to="/login" state={{ from: location }} replace />
  if (requirePermission && !hasPermission(requirePermission)) {
    return <Navigate to="/" replace />
  }
  return children
}
