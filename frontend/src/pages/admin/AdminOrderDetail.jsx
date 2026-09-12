import React, { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { orderApi } from '../../api/orderApi'
import { formatMoney, formatDateTime } from '../../utils/format'
import StatusBadge from '../../components/StatusBadge'
import ErrorBanner from '../../components/ErrorBanner'
import Spinner from '../../components/Spinner'

const ALL_STATUSES = ['PENDING', 'CONFIRMED', 'PROCESSING', 'PACKED', 'SHIPPED', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED', 'RETURN_REQUESTED', 'RETURNED', 'REFUNDED']

export default function AdminOrderDetail() {
  const { id } = useParams()
  const [order, setOrder] = useState(null)
  const [nextStatus, setNextStatus] = useState('')
  const [note, setNote] = useState('')
  const [trackingNumber, setTrackingNumber] = useState('')
  const [carrier, setCarrier] = useState('')
  const [error, setError] = useState('')

  function load() { orderApi.getById(id).then((o) => { setOrder(o); setNextStatus(o.status) }).catch(() => setOrder(false)) }
  useEffect(() => { load() }, [id])

  if (order === null) return <Spinner label="Loading order" />
  if (order === false) return <p className="text-ink-400">Order not found.</p>

  async function submitStatus(e) {
    e.preventDefault()
    setError('')
    try {
      const updated = await orderApi.updateStatus(order.id, { status: nextStatus, note, trackingNumber, carrier })
      setOrder(updated)
      setNote('')
    } catch (e) {
      setError(e.response?.data?.message || 'Could not update order status.')
    }
  }

  return (
    <div className="max-w-2xl">
      <Link to="/admin/orders" className="text-sm text-ink-400 hover:text-brass-600">← Back to orders</Link>
      <div className="flex items-center justify-between mt-3 mb-6">
        <h2 className="font-display text-2xl text-ink-900">{order.orderNumber}</h2>
        <StatusBadge status={order.status} />
      </div>

      <div className="rounded-md border border-ink-100 p-5 mb-6">
        <h3 className="font-medium text-ink-900 mb-3">Items</h3>
        <ul className="space-y-2 text-sm">
          {order.items.map((i, idx) => (
            <li key={idx} className="flex justify-between"><span className="text-ink-600">{i.productNameSnapshot} × {i.quantity} (SKU {i.sku})</span><span>{formatMoney(i.lineTotal)}</span></li>
          ))}
        </ul>
        <div className="mt-3 pt-3 border-t border-ink-100 flex justify-between font-semibold text-ink-900">
          <span>Total</span><span>{formatMoney(order.grandTotal)}</span>
        </div>
      </div>

      <form onSubmit={submitStatus} className="rounded-md border border-ink-100 p-5 mb-6 space-y-3">
        <h3 className="font-medium text-ink-900">Update status</h3>
        <ErrorBanner message={error} />
        <select value={nextStatus} onChange={(e) => setNextStatus(e.target.value)} className="w-full rounded border border-ink-100 px-3 py-2 text-sm">
          {ALL_STATUSES.map((s) => <option key={s} value={s}>{s.replaceAll('_', ' ')}</option>)}
        </select>
        <input placeholder="Note (optional)" value={note} onChange={(e) => setNote(e.target.value)} className="w-full rounded border border-ink-100 px-3 py-2 text-sm" />
        <div className="grid grid-cols-2 gap-3">
          <input placeholder="Tracking number" value={trackingNumber} onChange={(e) => setTrackingNumber(e.target.value)} className="rounded border border-ink-100 px-3 py-2 text-sm" />
          <input placeholder="Carrier" value={carrier} onChange={(e) => setCarrier(e.target.value)} className="rounded border border-ink-100 px-3 py-2 text-sm" />
        </div>
        <button type="submit" className="rounded bg-ink-900 text-linen px-5 py-2 text-sm">Update status</button>
        <p className="text-xs text-ink-400">Invalid transitions (e.g. skipping steps) are rejected by the server.</p>
      </form>

      <div className="rounded-md border border-ink-100 p-5">
        <h3 className="font-medium text-ink-900 mb-3">Status timeline</h3>
        <ol className="space-y-2 text-sm">
          {order.statusHistory?.map((e, idx) => (
            <li key={idx} className="flex justify-between text-ink-600">
              <span><StatusBadge status={e.status} /> {e.note}</span>
              <span className="text-ink-400">{formatDateTime(e.timestamp)}</span>
            </li>
          ))}
        </ol>
      </div>
    </div>
  )
}
