import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import ErrorBanner from '../components/ErrorBanner'

export default function RegisterPage() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ firstName: '', lastName: '', email: '', password: '', phone: '' })
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    if (form.password.length < 8) {
      setError('Password must be at least 8 characters.')
      return
    }
    setBusy(true)
    try {
      await register(form)
      navigate('/', { replace: true })
    } catch (e) {
      setError(e.response?.data?.message || 'Could not create your account.')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="mx-auto max-w-md px-4 py-16">
      <h1 className="font-display text-3xl text-ink-900 mb-2">Create your account</h1>
      <p className="text-sm text-ink-400 mb-8">Join Qavzuro to track orders, save favorites, and check out faster.</p>

      <form onSubmit={handleSubmit} className="space-y-4" noValidate>
        <ErrorBanner message={error} />
        <div className="grid grid-cols-2 gap-3">
          <div>
            <label htmlFor="firstName" className="text-sm font-medium text-ink-900">First name</label>
            <input id="firstName" required value={form.firstName} onChange={(e) => setForm({ ...form, firstName: e.target.value })}
                   className="mt-1 w-full rounded-md border border-ink-100 px-3 py-2.5 text-sm focus:border-brass-400" />
          </div>
          <div>
            <label htmlFor="lastName" className="text-sm font-medium text-ink-900">Last name</label>
            <input id="lastName" required value={form.lastName} onChange={(e) => setForm({ ...form, lastName: e.target.value })}
                   className="mt-1 w-full rounded-md border border-ink-100 px-3 py-2.5 text-sm focus:border-brass-400" />
          </div>
        </div>
        <div>
          <label htmlFor="email" className="text-sm font-medium text-ink-900">Email</label>
          <input id="email" type="email" required autoComplete="email" value={form.email}
                 onChange={(e) => setForm({ ...form, email: e.target.value })}
                 className="mt-1 w-full rounded-md border border-ink-100 px-3 py-2.5 text-sm focus:border-brass-400" />
        </div>
        <div>
          <label htmlFor="phone" className="text-sm font-medium text-ink-900">Phone (optional)</label>
          <input id="phone" value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })}
                 className="mt-1 w-full rounded-md border border-ink-100 px-3 py-2.5 text-sm focus:border-brass-400" />
        </div>
        <div>
          <label htmlFor="password" className="text-sm font-medium text-ink-900">Password</label>
          <input id="password" type="password" required minLength={8} autoComplete="new-password" value={form.password}
                 onChange={(e) => setForm({ ...form, password: e.target.value })}
                 className="mt-1 w-full rounded-md border border-ink-100 px-3 py-2.5 text-sm focus:border-brass-400" />
          <p className="mt-1 text-xs text-ink-400">At least 8 characters.</p>
        </div>
        <button type="submit" disabled={busy}
                className="w-full rounded-md bg-ink-900 px-6 py-3 text-sm font-semibold text-linen hover:bg-ink-800 disabled:opacity-50 transition-colors">
          {busy ? 'Creating account…' : 'Create account'}
        </button>
      </form>

      <p className="mt-6 text-sm text-ink-400">
        Already have an account? <Link to="/login" className="text-brass-600 hover:text-brass-500 font-medium">Sign in</Link>
      </p>
    </div>
  )
}
