import React, { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { orderApi } from '../api/orderApi'
import { formatMoney } from '../utils/format'
import StatusBadge from '../components/StatusBadge'
import Spinner from '../components/Spinner'

export default function OrderConfirmation() {
  const { orderNumber } = useParams()
  const [order, setOrder] = useState(null)

  useEffect(() => {
    orderApi.getByOrderNumber(orderNumber).then(setOrder).catch(() => setOrder(false))
  }, [orderNumber])

  if (order === null) return <Spinner label="Loading order" />
  if (order === false) return <div className="mx-auto max-w-2xl px-4 py-20 text-center text-ink-400">Order not found.</div>

  return (
    <div className="mx-auto max-w-2xl px-4 py-16 text-center">
      <div className="mx-auto mb-6 flex h-16 w-16 items-center justify-center rounded-full bg-green-100 text-green-600 text-3xl">✓</div>
      <h1 className="font-display text-3xl text-ink-900">Order confirmed</h1>
      <p className="mt-2 text-ink-600">Thanks for shopping with Qavzuro. Your order <strong>{order.orderNumber}</strong> is on its way.</p>

      <div className="mt-8 rounded-lg border border-ink-100 p-6 text-left">
        <div className="flex justify-between items-center mb-4">
          <StatusBadge status={order.status} />
          <span className="font-semibold text-ink-900">{formatMoney(order.grandTotal)}</span>
        </div>
        <ul className="space-y-2 text-sm">
          {order.items.map((i, idx) => (
            <li key={idx} className="flex justify-between">
              <span className="text-ink-600">{i.productNameSnapshot} × {i.quantity}</span>
              <span>{formatMoney(i.lineTotal)}</span>
            </li>
          ))}
        </ul>
      </div>

      <div className="mt-8 flex justify-center gap-4">
        <Link to="/account/orders" className="rounded-md border border-ink-900 px-6 py-3 text-sm font-medium">View your orders</Link>
        <Link to="/products" className="rounded-md bg-ink-900 px-6 py-3 text-sm font-medium text-linen">Continue shopping</Link>
      </div>
    </div>
  )
}
