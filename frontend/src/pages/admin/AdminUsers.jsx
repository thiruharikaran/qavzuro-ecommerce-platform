import React, { useEffect, useState } from 'react'
import { userApi } from '../../api/userApi'
import { adminApi } from '../../api/adminApi'
import { useAuth } from '../../context/AuthContext'
import ErrorBanner from '../../components/ErrorBanner'
import Spinner from '../../components/Spinner'

export default function AdminUsers() {
  const { hasPermission } = useAuth()
  const [result, setResult] = useState(null)
  const [roles, setRoles] = useState([])
  const [page, setPage] = useState(0)
  const [editingUser, setEditingUser] = useState(null)
  const [selectedRoles, setSelectedRoles] = useState([])
  const [error, setError] = useState('')

  function load() { userApi.list(page, 20).then(setResult).catch(() => setResult({ content: [] })) }
  useEffect(() => { load() }, [page])
  useEffect(() => { if (hasPermission('ROLE_MANAGE')) adminApi.roles().then(setRoles).catch(() => setRoles([])) }, [hasPermission])

  function startEdit(u) {
    setEditingUser(u)
    setSelectedRoles(u.roleCodes || [])
  }

  function toggleRole(code) {
    setSelectedRoles((prev) => prev.includes(code) ? prev.filter((r) => r !== code) : [...prev, code])
  }

  async function saveRoles() {
    setError('')
    try {
      await userApi.assignRoles(editingUser.id, selectedRoles)
      setEditingUser(null)
      load()
    } catch (e) {
      setError(e.response?.data?.message || 'Could not update roles.')
    }
  }

  async function toggleEnabled(u) {
    await userApi.setEnabled(u.id, !u.enabled)
    load()
  }

  if (!result) return <Spinner label="Loading users" />

  return (
    <div>
      <h2 className="font-medium text-ink-900 mb-4">Users</h2>
      <ErrorBanner message={error} />
      <div className="rounded-lg border border-ink-100 overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="bg-ink-50 text-ink-400 text-xs uppercase">
            <tr><th className="text-left px-4 py-2">Name</th><th className="text-left px-4 py-2">Email</th><th className="text-left px-4 py-2">Roles</th><th className="text-left px-4 py-2">Enabled</th><th></th></tr>
          </thead>
          <tbody>
            {result.content.map((u) => (
              <tr key={u.id} className="border-t border-ink-100">
                <td className="px-4 py-2 font-medium text-ink-900">{u.firstName} {u.lastName}</td>
                <td className="px-4 py-2 text-ink-400">{u.email}</td>
                <td className="px-4 py-2">{u.roleCodes?.join(', ')}</td>
                <td className="px-4 py-2">{u.enabled ? 'Yes' : 'No'}</td>
                <td className="px-4 py-2 text-right whitespace-nowrap">
                  {hasPermission('ROLE_MANAGE') && <button onClick={() => startEdit(u)} className="text-brass-600 hover:text-brass-500 mr-3">Edit roles</button>}
                  <button onClick={() => toggleEnabled(u)} className="text-ink-400 hover:text-ink-900">{u.enabled ? 'Disable' : 'Enable'}</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {result.totalPages > 1 && (
        <div className="mt-4 flex justify-center gap-2">
          {Array.from({ length: result.totalPages }).map((_, i) => (
            <button key={i} onClick={() => setPage(i)} className={`h-8 w-8 rounded text-sm ${i === page ? 'bg-ink-900 text-linen' : 'border border-ink-100'}`}>{i + 1}</button>
          ))}
        </div>
      )}

      {editingUser && (
        <div className="fixed inset-0 bg-ink-900/50 flex items-center justify-center p-4 z-50" role="dialog" aria-modal="true">
          <div className="bg-white rounded-lg p-6 max-w-md w-full">
            <h3 className="font-medium text-ink-900 mb-4">Edit roles — {editingUser.email}</h3>
            <div className="space-y-2 max-h-64 overflow-y-auto mb-4">
              {roles.map((r) => (
                <label key={r.code} className="flex items-center gap-2 text-sm">
                  <input type="checkbox" checked={selectedRoles.includes(r.code)} onChange={() => toggleRole(r.code)} />
                  {r.name} <span className="text-ink-400">({r.code})</span>
                </label>
              ))}
            </div>
            <div className="flex gap-3">
              <button onClick={saveRoles} className="rounded bg-ink-900 text-linen px-4 py-2 text-sm">Save</button>
              <button onClick={() => setEditingUser(null)} className="rounded border border-ink-100 px-4 py-2 text-sm">Cancel</button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
