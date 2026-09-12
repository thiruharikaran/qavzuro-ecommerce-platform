import React, { useEffect, useState } from 'react'
import { notificationApi } from '../../api/notificationApi'
import { formatDateTime } from '../../utils/format'
import EmptyState from '../../components/EmptyState'
import Spinner from '../../components/Spinner'

export default function AccountNotifications() {
  const [result, setResult] = useState(null)

  function load() { notificationApi.list(0, 30).then(setResult).catch(() => setResult({ content: [] })) }
  useEffect(() => { load() }, [])

  if (!result) return <Spinner label="Loading notifications" />

  async function markAllRead() {
    await notificationApi.markAllRead()
    load()
  }

  async function markRead(id) {
    await notificationApi.markRead(id)
    load()
  }

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <h2 className="font-medium text-ink-900">Notifications</h2>
        <button onClick={markAllRead} className="text-sm text-brass-600 hover:text-brass-500">Mark all as read</button>
      </div>
      {result.content.length === 0 ? (
        <EmptyState title="No notifications yet" />
      ) : (
        <ul className="space-y-2">
          {result.content.map((n) => (
            <li key={n.id} onClick={() => !n.read && markRead(n.id)}
                className={`rounded-md border p-4 text-sm cursor-pointer ${n.read ? 'border-ink-100' : 'border-brass-300 bg-brass-50'}`}>
              <p className="font-medium text-ink-900">{n.title}</p>
              <p className="text-ink-600 mt-1">{n.message}</p>
              <p className="text-xs text-ink-400 mt-1">{formatDateTime(n.createdAt)}</p>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
