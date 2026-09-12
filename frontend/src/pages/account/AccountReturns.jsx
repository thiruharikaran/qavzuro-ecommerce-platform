import React, { useEffect, useState } from 'react'
import { returnApi } from '../../api/returnApi'
import { formatDate } from '../../utils/format'
import StatusBadge from '../../components/StatusBadge'
import EmptyState from '../../components/EmptyState'
import Spinner from '../../components/Spinner'

export default function AccountReturns() {
  const [result, setResult] = useState(null)
  const [page, setPage] = useState(0)

  useEffect(() => { returnApi.mine(page, 10).then(setResult).catch(() => setResult({ content: [] })) }, [page])

  if (!result) return <Spinner label="Loading returns" />

  return (
    <div>
      <h2 className="font-medium text-ink-900 mb-4">Returns & refunds</h2>
      {result.content.length === 0 ? (
        <EmptyState title="No return requests" description="Returns you request will appear here with their status." />
      ) : (
        <ul className="space-y-3">
          {result.content.map((r) => (
            <li key={r.id} className="rounded-md border border-ink-100 p-4 text-sm">
              <div className="flex justify-between items-start">
                <div>
                  <p className="font-medium text-ink-900">Reason: {r.reason}</p>
                  <p className="text-ink-400 text-xs mt-1">Requested {formatDate(r.createdAt)}</p>
                  {r.reviewNote && <p className="text-ink-600 mt-1">Staff note: {r.reviewNote}</p>}
                  {r.refundAmount != null && <p className="text-green-700 mt-1">Refund: ₹{r.refundAmount}</p>}
                </div>
                <StatusBadge status={r.status} />
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
