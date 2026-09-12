import React, { useEffect, useState } from 'react'
import { adminApi } from '../../api/adminApi'
import ErrorBanner from '../../components/ErrorBanner'
import Spinner from '../../components/Spinner'

export default function AdminRoles() {
  const [roles, setRoles] = useState(null)
  const [permissions, setPermissions] = useState([])
  const [selectedRole, setSelectedRole] = useState(null)
  const [checkedPerms, setCheckedPerms] = useState([])
  const [newRole, setNewRole] = useState({ code: '', name: '', description: '' })
  const [error, setError] = useState('')

  function load() {
    adminApi.roles().then(setRoles).catch(() => setRoles([]))
    adminApi.permissions().then(setPermissions).catch(() => setPermissions([]))
  }
  useEffect(() => { load() }, [])

  function selectRole(r) {
    setSelectedRole(r)
    setCheckedPerms(Array.from(r.permissionCodes || []))
  }

  function togglePerm(code) {
    setCheckedPerms((prev) => prev.includes(code) ? prev.filter((p) => p !== code) : [...prev, code])
  }

  async function savePermissions() {
    setError('')
    try {
      await adminApi.updateRolePermissions(selectedRole.id, checkedPerms)
      setSelectedRole(null)
      load()
    } catch (e) {
      setError(e.response?.data?.message || 'Could not update role permissions.')
    }
  }

  async function createRole(e) {
    e.preventDefault()
    setError('')
    try {
      await adminApi.createRole({ ...newRole, permissionCodes: [] })
      setNewRole({ code: '', name: '', description: '' })
      load()
    } catch (e) {
      setError(e.response?.data?.message || 'Could not create role.')
    }
  }

  if (!roles) return <Spinner label="Loading roles" />

  const grouped = permissions.reduce((acc, p) => { (acc[p.category] ||= []).push(p); return acc }, {})

  return (
    <div>
      <h2 className="font-medium text-ink-900 mb-4">Roles & permissions</h2>
      <ErrorBanner message={error} />

      <div className="grid md:grid-cols-2 gap-6 mb-8">
        {roles.map((r) => (
          <button key={r.id} onClick={() => selectRole(r)}
                  className={`text-left rounded-md border p-4 ${selectedRole?.id === r.id ? 'border-brass-500 bg-brass-50' : 'border-ink-100'}`}>
            <div className="flex justify-between">
              <span className="font-medium text-ink-900">{r.name}</span>
              {r.system && <span className="text-xs text-ink-400">system</span>}
            </div>
            <p className="text-xs text-ink-400 mt-1">{r.permissionCodes?.length || 0} permissions</p>
          </button>
        ))}
      </div>

      <form onSubmit={createRole} className="mb-8 flex flex-wrap gap-3 items-end rounded-md border border-ink-100 p-4">
        <div>
          <label className="text-xs text-ink-400">Code</label>
          <input required value={newRole.code} onChange={(e) => setNewRole({ ...newRole, code: e.target.value.toUpperCase() })} className="block rounded border border-ink-100 px-3 py-2 text-sm" />
        </div>
        <div>
          <label className="text-xs text-ink-400">Name</label>
          <input required value={newRole.name} onChange={(e) => setNewRole({ ...newRole, name: e.target.value })} className="block rounded border border-ink-100 px-3 py-2 text-sm" />
        </div>
        <div className="flex-1 min-w-[160px]">
          <label className="text-xs text-ink-400">Description</label>
          <input value={newRole.description} onChange={(e) => setNewRole({ ...newRole, description: e.target.value })} className="block w-full rounded border border-ink-100 px-3 py-2 text-sm" />
        </div>
        <button type="submit" className="rounded bg-ink-900 text-linen px-4 py-2 text-sm h-fit">Create role</button>
      </form>

      {selectedRole && (
        <div className="rounded-md border border-ink-100 p-5">
          <h3 className="font-medium text-ink-900 mb-4">Permissions for {selectedRole.name}</h3>
          {Object.entries(grouped).map(([category, perms]) => (
            <div key={category} className="mb-4">
              <p className="text-xs font-semibold uppercase text-ink-400 mb-2">{category}</p>
              <div className="grid grid-cols-2 md:grid-cols-3 gap-2">
                {perms.map((p) => (
                  <label key={p.code} className="flex items-center gap-2 text-sm">
                    <input type="checkbox" checked={checkedPerms.includes(p.code)} onChange={() => togglePerm(p.code)} />
                    {p.code}
                  </label>
                ))}
              </div>
            </div>
          ))}
          <button onClick={savePermissions} className="mt-2 rounded bg-ink-900 text-linen px-5 py-2 text-sm">Save permissions</button>
        </div>
      )}
    </div>
  )
}
