import React, { useEffect, useState } from 'react'
import { returnApi } from '../../api/returnApi'
import { formatDate } from '../../utils/format'
import StatusBadge from '../../components/StatusBadge'
import ErrorBanner from '../../components/ErrorBanner'
import Spinner from '../../components/Spinner'

const ACTIONS = ['APPROVED', 'REJECTED', 'ITEM_RECEIVED', 'REFUNDED', 'CLOSED']

export default function AdminReturns() {
  const [result, setResult] = useState(null)
  const [error, setError] = useState('')
  const [noteDrafts, setNoteDrafts] = useState({})

  function load() { returnApi.listAll(0, 50).then(setResult).catch(() => setResult({ content: [] })) }
  useEffect(() => { load() }, [])

  if (!result) return <Spinner label="Loading return requests" />

  async function act(id, status) {
    setError('')
    try {
      await returnApi.review(id, { status, reviewNote: noteDrafts[id] || '' })
      load()
    } catch (e) {
      setError(e.response?.data?.message || 'Could not process return.')
    }
  }

  return (
    <div>
      <h2 className="font-medium text-ink-900 mb-4">Return requests</h2>
      <ErrorBanner message={error} />
      <ul className="space-y-4">
        {result.content.map((r) => (
          <li key={r.id} className="rounded-md border border-ink-100 p-4">
            <div className="flex justify-between items-start mb-2">
              <div>
                <p className="font-medium text-ink-900">Order {r.orderId}</p>
                <p className="text-sm text-ink-600">Reason: {r.reason}</p>
                <p className="text-xs text-ink-400">{formatDate(r.createdAt)}</p>
              </div>
              <StatusBadge status={r.status} />
            </div>
            {r.status === 'REQUESTED' || r.status === 'APPROVED' ? (
              <div className="mt-3 flex flex-wrap gap-2 items-center">
                <input placeholder="Note" value={noteDrafts[r.id] || ''} onChange={(e) => setNoteDrafts({ ...noteDrafts, [r.id]: e.target.value })}
                       className="rounded border border-ink-100 px-2 py-1 text-sm flex-1 min-w-[160px]" />
                {ACTIONS.map((a) => (
                  <button key={a} onClick={() => act(r.id, a)} className="rounded border border-ink-900 px-3 py-1 text-xs hover:bg-ink-50">
                    {a.replaceAll('_', ' ')}
                  </button>
                ))}
              </div>
            ) : null}
          </li>
        ))}
      </ul>
    </div>
  )
}
