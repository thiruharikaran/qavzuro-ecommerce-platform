import React, { useEffect, useState } from 'react'
import { workforceApi } from '../../api/workforceApi'
import { useAuth } from '../../context/AuthContext'
import { formatDate } from '../../utils/format'
import EmptyState from '../../components/EmptyState'
import Spinner from '../../components/Spinner'
import ErrorBanner from '../../components/ErrorBanner'

const STATUS_OPTIONS = ['OPEN', 'IN_PROGRESS', 'DONE', 'CANCELLED']

export default function WorkforceDashboard() {
  const { user, hasPermission } = useAuth()
  const [myTasks, setMyTasks] = useState(null)
  const [assignedByMe, setAssignedByMe] = useState(null)
  const [error, setError] = useState('')

  const canAssign = hasPermission('WORKFORCE_TASK_ASSIGN')

  function loadMine() { workforceApi.myTasks(0, 50).then(setMyTasks).catch(() => setMyTasks({ content: [] })) }
  function loadAssigned() { canAssign && workforceApi.tasksIAssigned(0, 50).then(setAssignedByMe).catch(() => setAssignedByMe({ content: [] })) }

  useEffect(() => { loadMine(); loadAssigned() }, [])

  async function updateStatus(id, status) {
    setError('')
    try {
      await workforceApi.updateStatus(id, status)
      loadMine()
    } catch (e) {
      setError(e.response?.data?.message || 'Could not update task status.')
    }
  }

  return (
    <div className="mx-auto max-w-5xl px-4 py-10">
      <h1 className="font-display text-3xl text-ink-900 mb-1">Workforce dashboard</h1>
      <p className="text-sm text-ink-400 mb-8">Signed in as {user?.firstName} — roles: {user?.roleCodes?.join(', ')}</p>

      <ErrorBanner message={error} />

      <section className="mb-10">
        <h2 className="font-medium text-ink-900 mb-4">Tasks assigned to me</h2>
        {myTasks === null ? <Spinner label="Loading tasks" /> : myTasks.content.length === 0 ? (
          <EmptyState title="No tasks assigned" description="Tasks assigned to you by a supervisor or manager will appear here." />
        ) : (
          <ul className="space-y-3">
            {myTasks.content.map((t) => (
              <li key={t.id} className="rounded-md border border-ink-100 p-4 flex items-center justify-between gap-4">
                <div>
                  <p className="font-medium text-ink-900">{t.title}</p>
                  <p className="text-sm text-ink-600">{t.description}</p>
                  <p className="text-xs text-ink-400 mt-1">Type: {t.taskType} {t.dueAt && `· Due ${formatDate(t.dueAt)}`}</p>
                </div>
                <select value={t.status} onChange={(e) => updateStatus(t.id, e.target.value)}
                        className="rounded border border-ink-100 px-2 py-1.5 text-sm shrink-0">
                  {STATUS_OPTIONS.map((s) => <option key={s} value={s}>{s.replaceAll('_', ' ')}</option>)}
                </select>
              </li>
            ))}
          </ul>
        )}
      </section>

      {canAssign && (
        <section>
          <h2 className="font-medium text-ink-900 mb-4">Tasks I've assigned</h2>
          {assignedByMe === null ? <Spinner label="Loading" /> : assignedByMe.content.length === 0 ? (
            <EmptyState title="You haven't assigned any tasks yet" description="Use the admin Workforce section to assign tasks to your team." />
          ) : (
            <ul className="space-y-3">
              {assignedByMe.content.map((t) => (
                <li key={t.id} className="rounded-md border border-ink-100 p-4">
                  <p className="font-medium text-ink-900">{t.title}</p>
                  <p className="text-xs text-ink-400 mt-1">Assigned to {t.assignedToUserId} · Status: {t.status}</p>
                </li>
              ))}
            </ul>
          )}
        </section>
      )}
    </div>
  )
}
