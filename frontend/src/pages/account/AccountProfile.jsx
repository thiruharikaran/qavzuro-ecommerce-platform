import React, { useEffect, useState } from 'react'
import { useAuth } from '../../context/AuthContext'
import { userApi } from '../../api/userApi'
import ErrorBanner from '../../components/ErrorBanner'

export default function AccountProfile() {
  const { user, setUser } = useAuth()
  const [form, setForm] = useState({ firstName: '', lastName: '', phone: '' })
  const [pwForm, setPwForm] = useState({ currentPassword: '', newPassword: '' })
  const [profileMsg, setProfileMsg] = useState('')
  const [profileError, setProfileError] = useState('')
  const [pwMsg, setPwMsg] = useState('')
  const [pwError, setPwError] = useState('')

  useEffect(() => {
    if (user) setForm({ firstName: user.firstName || '', lastName: user.lastName || '', phone: user.phone || '' })
  }, [user])

  async function saveProfile(e) {
    e.preventDefault()
    setProfileError(''); setProfileMsg('')
    try {
      const updated = await userApi.updateProfile(form)
      setUser(updated)
      setProfileMsg('Profile updated.')
    } catch (e) {
      setProfileError(e.response?.data?.message || 'Could not update profile.')
    }
  }

  async function changePassword(e) {
    e.preventDefault()
    setPwError(''); setPwMsg('')
    if (pwForm.newPassword.length < 8) { setPwError('New password must be at least 8 characters.'); return }
    try {
      await userApi.changePassword(pwForm)
      setPwMsg('Password changed successfully.')
      setPwForm({ currentPassword: '', newPassword: '' })
    } catch (e) {
      setPwError(e.response?.data?.message || 'Could not change password.')
    }
  }

  return (
    <div className="space-y-10 max-w-lg">
      <section>
        <h2 className="font-medium text-ink-900 mb-4">Profile details</h2>
        <form onSubmit={saveProfile} className="space-y-3">
          <ErrorBanner message={profileError} />
          {profileMsg && <p className="text-sm text-green-700">{profileMsg}</p>}
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label htmlFor="firstName" className="text-sm font-medium text-ink-900">First name</label>
              <input id="firstName" value={form.firstName} onChange={(e) => setForm({ ...form, firstName: e.target.value })}
                     className="mt-1 w-full rounded-md border border-ink-100 px-3 py-2 text-sm" />
            </div>
            <div>
              <label htmlFor="lastName" className="text-sm font-medium text-ink-900">Last name</label>
              <input id="lastName" value={form.lastName} onChange={(e) => setForm({ ...form, lastName: e.target.value })}
                     className="mt-1 w-full rounded-md border border-ink-100 px-3 py-2 text-sm" />
            </div>
          </div>
          <div>
            <label htmlFor="phone" className="text-sm font-medium text-ink-900">Phone</label>
            <input id="phone" value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })}
                   className="mt-1 w-full rounded-md border border-ink-100 px-3 py-2 text-sm" />
          </div>
          <button type="submit" className="rounded-md bg-ink-900 px-5 py-2.5 text-sm font-medium text-linen hover:bg-ink-800">Save changes</button>
        </form>
      </section>

      <section>
        <h2 className="font-medium text-ink-900 mb-4">Change password</h2>
        <form onSubmit={changePassword} className="space-y-3">
          <ErrorBanner message={pwError} />
          {pwMsg && <p className="text-sm text-green-700">{pwMsg}</p>}
          <div>
            <label htmlFor="currentPassword" className="text-sm font-medium text-ink-900">Current password</label>
            <input id="currentPassword" type="password" autoComplete="current-password" value={pwForm.currentPassword}
                   onChange={(e) => setPwForm({ ...pwForm, currentPassword: e.target.value })}
                   className="mt-1 w-full rounded-md border border-ink-100 px-3 py-2 text-sm" />
          </div>
          <div>
            <label htmlFor="newPassword" className="text-sm font-medium text-ink-900">New password</label>
            <input id="newPassword" type="password" autoComplete="new-password" minLength={8} value={pwForm.newPassword}
                   onChange={(e) => setPwForm({ ...pwForm, newPassword: e.target.value })}
                   className="mt-1 w-full rounded-md border border-ink-100 px-3 py-2 text-sm" />
          </div>
          <button type="submit" className="rounded-md border border-ink-900 px-5 py-2.5 text-sm font-medium hover:bg-ink-50">Update password</button>
        </form>
      </section>
    </div>
  )
}
