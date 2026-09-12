import React, { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { orderApi } from '../../api/orderApi'
import { formatMoney, formatDate } from '../../utils/format'
import StatusBadge from '../../components/StatusBadge'
import Spinner from '../../components/Spinner'

export default function AdminOrders() {
  const [result, setResult] = useState(null)
  const [page, setPage] = useState(0)

  useEffect(() => { orderApi.listAll(page, 20).then(setResult).catch(() => setResult({ content: [] })) }, [page])

  if (!result) return <Spinner label="Loading orders" />

  return (
    <div>
      <h2 className="font-medium text-ink-900 mb-4">All orders</h2>
      <div className="rounded-lg border border-ink-100 overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="bg-ink-50 text-ink-400 text-xs uppercase">
            <tr><th className="text-left px-4 py-2">Order</th><th className="text-left px-4 py-2">Status</th><th className="text-left px-4 py-2">Payment</th><th className="text-left px-4 py-2">Total</th><th className="text-left px-4 py-2">Date</th><th></th></tr>
          </thead>
          <tbody>
            {result.content.map((o) => (
              <tr key={o.id} className="border-t border-ink-100">
                <td className="px-4 py-2 font-medium text-ink-900">{o.orderNumber}</td>
                <td className="px-4 py-2"><StatusBadge status={o.status} /></td>
                <td className="px-4 py-2"><StatusBadge status={o.paymentStatus} /></td>
                <td className="px-4 py-2">{formatMoney(o.grandTotal)}</td>
                <td className="px-4 py-2 text-ink-400">{formatDate(o.createdAt)}</td>
                <td className="px-4 py-2 text-right"><Link to={`/admin/orders/${o.id}`} className="text-brass-600 hover:text-brass-500">Manage</Link></td>
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
    </div>
  )
}
