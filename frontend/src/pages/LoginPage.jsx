import React, { useState } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import ErrorBanner from '../components/ErrorBanner'

export default function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [form, setForm] = useState({ email: '', password: '' })
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setBusy(true)
    try {
      await login(form.email, form.password)
      const redirectTo = location.state?.from?.pathname || '/'
      navigate(redirectTo, { replace: true })
    } catch (e) {
      setError(e.response?.data?.message || 'Invalid email or password.')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="mx-auto max-w-md px-4 py-16">
      <h1 className="font-display text-3xl text-ink-900 mb-2">Welcome back</h1>
      <p className="text-sm text-ink-400 mb-8">Sign in to your Qavzuro account.</p>

      <form onSubmit={handleSubmit} className="space-y-4" noValidate>
        <ErrorBanner message={error} />
        <div>
          <label htmlFor="email" className="text-sm font-medium text-ink-900">Email</label>
          <input id="email" type="email" required autoComplete="email" value={form.email}
                 onChange={(e) => setForm({ ...form, email: e.target.value })}
                 className="mt-1 w-full rounded-md border border-ink-100 px-3 py-2.5 text-sm focus:border-brass-400" />
        </div>
        <div>
          <label htmlFor="password" className="text-sm font-medium text-ink-900">Password</label>
          <input id="password" type="password" required autoComplete="current-password" value={form.password}
                 onChange={(e) => setForm({ ...form, password: e.target.value })}
                 className="mt-1 w-full rounded-md border border-ink-100 px-3 py-2.5 text-sm focus:border-brass-400" />
        </div>
        <button type="submit" disabled={busy}
                className="w-full rounded-md bg-ink-900 px-6 py-3 text-sm font-semibold text-linen hover:bg-ink-800 disabled:opacity-50 transition-colors">
          {busy ? 'Signing in…' : 'Sign in'}
        </button>
      </form>

      <p className="mt-6 text-sm text-ink-400">
        New to Qavzuro? <Link to="/register" className="text-brass-600 hover:text-brass-500 font-medium">Create an account</Link>
      </p>

      <div className="mt-10 rounded-md border border-ink-100 bg-ink-50 p-4 text-xs text-ink-400">
        <p className="font-medium text-ink-600 mb-1">Sample accounts (dev seed data):</p>
        <p>customer@qavzuro.dev / Customer123!</p>
        <p>manager@qavzuro.dev / Manager123!</p>
        <p>worker@qavzuro.dev / Worker123!</p>
      </div>
    </div>
  )
}
