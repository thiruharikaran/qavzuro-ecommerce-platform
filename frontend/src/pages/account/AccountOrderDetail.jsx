import React, { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { orderApi } from '../../api/orderApi'
import { returnApi } from '../../api/returnApi'
import { formatMoney, formatDateTime } from '../../utils/format'
import StatusBadge from '../../components/StatusBadge'
import ErrorBanner from '../../components/ErrorBanner'
import Spinner from '../../components/Spinner'

export default function AccountOrderDetail() {
  const { id } = useParams()
  const [order, setOrder] = useState(null)
  const [error, setError] = useState('')
  const [returnReason, setReturnReason] = useState('')
  const [returnMsg, setReturnMsg] = useState('')
  const [showReturnForm, setShowReturnForm] = useState(false)

  function load() { orderApi.getById(id).then(setOrder).catch(() => setOrder(false)) }
  useEffect(() => { load() }, [id])

  if (order === null) return <Spinner label="Loading order" />
  if (order === false) return <p className="text-ink-400">Order not found.</p>

  async function cancelOrder() {
    setError('')
    try {
      const updated = await orderApi.cancel(order.id)
      setOrder(updated)
    } catch (e) {
      setError(e.response?.data?.message || 'Could not cancel order.')
    }
  }

  async function requestReturn(e) {
    e.preventDefault()
    setError(''); setReturnMsg('')
    try {
      await returnApi.create({ orderId: order.id, reason: returnReason, orderItemProductIds: [] })
      setReturnMsg('Return request submitted.')
      setShowReturnForm(false)
      load()
    } catch (e) {
      setError(e.response?.data?.message || 'Could not submit return request.')
    }
  }

  const canCancel = order.status === 'PENDING' || order.status === 'CONFIRMED'
  const canReturn = order.status === 'DELIVERED'

  return (
    <div className="max-w-2xl">
      <Link to="/account/orders" className="text-sm text-ink-400 hover:text-brass-600">← Back to orders</Link>
      <div className="flex items-center justify-between mt-3 mb-6">
        <h2 className="font-display text-2xl text-ink-900">{order.orderNumber}</h2>
        <StatusBadge status={order.status} />
      </div>

      <ErrorBanner message={error} />
      {returnMsg && <p className="text-sm text-green-700 mb-3">{returnMsg}</p>}

      <div className="rounded-md border border-ink-100 p-5 mb-6">
        <h3 className="font-medium text-ink-900 mb-3">Items</h3>
        <ul className="space-y-2 text-sm">
          {order.items.map((i, idx) => (
            <li key={idx} className="flex justify-between">
              <span className="text-ink-600">{i.productNameSnapshot} × {i.quantity}</span>
              <span>{formatMoney(i.lineTotal)}</span>
            </li>
          ))}
        </ul>
        <div className="mt-4 pt-4 border-t border-ink-100 space-y-1 text-sm">
          <div className="flex justify-between text-ink-400"><span>Subtotal</span><span>{formatMoney(order.subtotal)}</span></div>
          {order.discountTotal > 0 && <div className="flex justify-between text-green-700"><span>Discount</span><span>-{formatMoney(order.discountTotal)}</span></div>}
          <div className="flex justify-between text-ink-400"><span>Tax</span><span>{formatMoney(order.taxTotal)}</span></div>
          <div className="flex justify-between text-ink-400"><span>Shipping</span><span>{formatMoney(order.shippingTotal)}</span></div>
          <div className="flex justify-between font-semibold text-ink-900 pt-1"><span>Total</span><span>{formatMoney(order.grandTotal)}</span></div>
        </div>
      </div>

      <div className="grid md:grid-cols-2 gap-4 mb-6 text-sm">
        <div className="rounded-md border border-ink-100 p-4">
          <h3 className="font-medium text-ink-900 mb-2">Shipping address</h3>
          <p className="text-ink-600">{order.shippingAddressSnapshot?.fullName}</p>
          <p className="text-ink-600">{order.shippingAddressSnapshot?.line1}, {order.shippingAddressSnapshot?.city}, {order.shippingAddressSnapshot?.state} {order.shippingAddressSnapshot?.postalCode}</p>
        </div>
        <div className="rounded-md border border-ink-100 p-4">
          <h3 className="font-medium text-ink-900 mb-2">Tracking</h3>
          <p className="text-ink-600">{order.trackingNumber ? `${order.carrier || ''} ${order.trackingNumber}` : 'Not yet shipped'}</p>
        </div>
      </div>

      <div className="rounded-md border border-ink-100 p-5 mb-6">
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

      <div className="flex gap-3">
        {canCancel && <button onClick={cancelOrder} className="rounded-md border border-red-300 text-red-700 px-5 py-2.5 text-sm hover:bg-red-50">Cancel order</button>}
        {canReturn && !showReturnForm && <button onClick={() => setShowReturnForm(true)} className="rounded-md border border-ink-900 px-5 py-2.5 text-sm">Request return</button>}
      </div>

      {showReturnForm && (
        <form onSubmit={requestReturn} className="mt-4 space-y-3 max-w-md">
          <textarea required placeholder="Reason for return" value={returnReason} onChange={(e) => setReturnReason(e.target.value)}
                    className="w-full rounded border border-ink-100 px-3 py-2 text-sm" rows={3} />
          <div className="flex gap-3">
            <button type="submit" className="rounded bg-ink-900 text-linen px-5 py-2 text-sm">Submit return request</button>
            <button type="button" onClick={() => setShowReturnForm(false)} className="rounded border border-ink-100 px-5 py-2 text-sm">Cancel</button>
          </div>
        </form>
      )}
    </div>
  )
}
