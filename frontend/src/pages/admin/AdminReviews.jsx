import React, { useEffect, useState } from 'react'
import { reviewApi } from '../../api/reviewApi'
import { formatDate } from '../../utils/format'
import EmptyState from '../../components/EmptyState'
import Spinner from '../../components/Spinner'

export default function AdminReviews() {
  const [result, setResult] = useState(null)

  function load() { reviewApi.pending(0, 50).then(setResult).catch(() => setResult({ content: [] })) }
  useEffect(() => { load() }, [])

  if (!result) return <Spinner label="Loading reviews" />

  async function moderate(id, status) {
    await reviewApi.moderate(id, status)
    load()
  }

  return (
    <div>
      <h2 className="font-medium text-ink-900 mb-4">Reviews pending moderation</h2>
      {result.content.length === 0 ? (
        <EmptyState title="Nothing to moderate" description="New reviews will appear here for approval." />
      ) : (
        <ul className="space-y-4">
          {result.content.map((r) => (
            <li key={r.id} className="rounded-md border border-ink-100 p-4">
              <div className="flex items-center gap-2">
                <span className="text-brass-500">{'★'.repeat(r.rating)}{'☆'.repeat(5 - r.rating)}</span>
                <span className="font-medium text-ink-900">{r.title}</span>
                {r.verifiedPurchase && <span className="text-xs text-green-700 bg-green-50 px-2 py-0.5 rounded">Verified</span>}
              </div>
              <p className="text-sm text-ink-600 mt-1">{r.reviewText}</p>
              <p className="text-xs text-ink-400 mt-1">{r.customerDisplayName} · {formatDate(r.createdAt)}</p>
              <div className="mt-3 flex gap-2">
                <button onClick={() => moderate(r.id, 'APPROVED')} className="rounded border border-green-600 text-green-700 px-3 py-1 text-xs hover:bg-green-50">Approve</button>
                <button onClick={() => moderate(r.id, 'REJECTED')} className="rounded border border-red-400 text-red-700 px-3 py-1 text-xs hover:bg-red-50">Reject</button>
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
