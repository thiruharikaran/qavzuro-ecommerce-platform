import React, { useEffect, useState } from 'react'
import { workforceApi } from '../../api/workforceApi'
import { userApi } from '../../api/userApi'
import { formatDate } from '../../utils/format'
import ErrorBanner from '../../components/ErrorBanner'
import Spinner from '../../components/Spinner'

const TASK_TYPES = ['ORDER_PACKING', 'INVENTORY_CHECK', 'CLEANING', 'GENERAL']
const WORKFORCE_ROLES = ['WORKER', 'CLEANER', 'SUPERVISOR', 'TEAM_LEAD', 'MANAGER']

export default function AdminWorkforce() {
  const [tasks, setTasks] = useState(null)
  const [workforceUsers, setWorkforceUsers] = useState([])
  const [form, setForm] = useState({ title: '', description: '', taskType: 'GENERAL', assignedToUserId: '', dueAt: '' })
  const [error, setError] = useState('')

  function load() { workforceApi.tasksIAssigned(0, 50).then(setTasks).catch(() => setTasks({ content: [] })) }
  useEffect(() => {
    load()
    userApi.list(0, 100).then((res) => {
      setWorkforceUsers(res.content.filter((u) => u.roleCodes?.some((r) => WORKFORCE_ROLES.includes(r))))
    }).catch(() => setWorkforceUsers([]))
  }, [])

  async function assign(e) {
    e.preventDefault()
    setError('')
    try {
      await workforceApi.assign({ ...form, dueAt: form.dueAt ? new Date(form.dueAt).toISOString() : null })
      setForm({ title: '', description: '', taskType: 'GENERAL', assignedToUserId: '', dueAt: '' })
      load()
    } catch (e) {
      setError(e.response?.data?.message || 'Could not assign task.')
    }
  }

  if (!tasks) return <Spinner label="Loading workforce" />

  return (
    <div className="grid md:grid-cols-[1fr_360px] gap-8">
      <div>
        <h2 className="font-medium text-ink-900 mb-4">Tasks assigned by me</h2>
        {tasks.content.length === 0 ? (
          <p className="text-sm text-ink-400">No tasks assigned yet.</p>
        ) : (
          <ul className="space-y-2">
            {tasks.content.map((t) => (
              <li key={t.id} className="rounded-md border border-ink-100 p-3 text-sm">
                <p className="font-medium text-ink-900">{t.title}</p>
                <p className="text-ink-400 text-xs mt-1">Assigned to: {t.assignedToUserId} · Status: {t.status} {t.dueAt && `· Due ${formatDate(t.dueAt)}`}</p>
              </li>
            ))}
          </ul>
        )}
      </div>

      <form onSubmit={assign} className="space-y-3 rounded-lg border border-ink-100 p-4 h-fit">
        <h3 className="font-medium text-ink-900">Assign new task</h3>
        <ErrorBanner message={error} />
        <input required placeholder="Title" value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} className="w-full rounded border border-ink-100 px-3 py-2 text-sm" />
        <textarea placeholder="Description" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} className="w-full rounded border border-ink-100 px-3 py-2 text-sm" rows={2} />
        <select value={form.taskType} onChange={(e) => setForm({ ...form, taskType: e.target.value })} className="w-full rounded border border-ink-100 px-3 py-2 text-sm">
          {TASK_TYPES.map((t) => <option key={t} value={t}>{t.replaceAll('_', ' ')}</option>)}
        </select>
        <select required value={form.assignedToUserId} onChange={(e) => setForm({ ...form, assignedToUserId: e.target.value })} className="w-full rounded border border-ink-100 px-3 py-2 text-sm">
          <option value="">Assign to…</option>
          {workforceUsers.map((u) => <option key={u.id} value={u.id}>{u.firstName} {u.lastName} ({u.roleCodes?.join(', ')})</option>)}
        </select>
        <input type="date" value={form.dueAt} onChange={(e) => setForm({ ...form, dueAt: e.target.value })} className="w-full rounded border border-ink-100 px-3 py-2 text-sm" />
        <button type="submit" className="rounded bg-ink-900 text-linen px-4 py-2 text-sm">Assign task</button>
      </form>
    </div>
  )
}
