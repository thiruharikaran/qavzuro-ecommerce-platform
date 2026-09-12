import React, { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { orderApi } from '../../api/orderApi'
import { formatMoney, formatDate } from '../../utils/format'
import StatusBadge from '../../components/StatusBadge'
import EmptyState from '../../components/EmptyState'
import Spinner from '../../components/Spinner'

export default function AccountOrders() {
  const [result, setResult] = useState(null)
  const [page, setPage] = useState(0)

  useEffect(() => { orderApi.myOrders(page, 10).then(setResult).catch(() => setResult({ content: [] })) }, [page])

  if (!result) return <Spinner label="Loading orders" />

  return (
    <div>
      <h2 className="font-medium text-ink-900 mb-4">Order history</h2>
      {result.content.length === 0 ? (
        <EmptyState title="No orders yet" description="Once you place an order, it will show up here."
          action={<Link to="/products" className="rounded-md bg-ink-900 px-6 py-3 text-sm font-medium text-linen">Start shopping</Link>} />
      ) : (
        <>
          <ul className="space-y-3">
            {result.content.map((o) => (
              <li key={o.id}>
                <Link to={`/account/orders/${o.id}`} className="flex items-center justify-between rounded-md border border-ink-100 p-4 hover:border-brass-400 transition-colors">
                  <div>
                    <p className="font-medium text-ink-900">{o.orderNumber}</p>
                    <p className="text-xs text-ink-400">{formatDate(o.createdAt)} · {o.items.length} item(s)</p>
                  </div>
                  <div className="flex items-center gap-4">
                    <StatusBadge status={o.status} />
                    <span className="font-medium text-ink-900">{formatMoney(o.grandTotal)}</span>
                  </div>
                </Link>
              </li>
            ))}
          </ul>
          {result.totalPages > 1 && (
            <div className="mt-6 flex justify-center gap-2">
              {Array.from({ length: result.totalPages }).map((_, i) => (
                <button key={i} onClick={() => setPage(i)} className={`h-8 w-8 rounded text-sm ${i === page ? 'bg-ink-900 text-linen' : 'border border-ink-100'}`}>{i + 1}</button>
              ))}
            </div>
          )}
        </>
      )}
    </div>
  )
}
