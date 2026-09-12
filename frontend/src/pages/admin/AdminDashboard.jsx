import React, { useEffect, useState } from 'react'
import { adminApi } from '../../api/adminApi'
import { formatMoney, formatDate } from '../../utils/format'
import Spinner from '../../components/Spinner'

function StatCard({ label, value }) {
  return (
    <div className="rounded-lg border border-ink-100 p-5">
      <p className="text-xs uppercase tracking-wide text-ink-400">{label}</p>
      <p className="mt-2 text-2xl font-semibold text-ink-900">{value}</p>
    </div>
  )
}

export default function AdminDashboard() {
  const [stats, setStats] = useState(null)

  useEffect(() => { adminApi.dashboard().then(setStats).catch(() => setStats(false)) }, [])

  if (stats === null) return <Spinner label="Loading dashboard" />
  if (stats === false) return <p className="text-ink-400">Could not load dashboard stats.</p>

  return (
    <div>
      <div className="grid grid-cols-2 md:grid-cols-5 gap-4 mb-10">
        <StatCard label="Revenue" value={formatMoney(stats.totalRevenue)} />
        <StatCard label="Orders" value={stats.totalOrders} />
        <StatCard label="Customers" value={stats.totalCustomers} />
        <StatCard label="Pending returns" value={stats.pendingReturns} />
        <StatCard label="Low stock items" value={stats.lowStockProductCount} />
      </div>

      <h2 className="font-medium text-ink-900 mb-3">Recent orders</h2>
      <div className="rounded-lg border border-ink-100 overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-ink-50 text-ink-400 text-xs uppercase">
            <tr><th className="text-left px-4 py-2">Order</th><th className="text-left px-4 py-2">Status</th><th className="text-left px-4 py-2">Total</th><th className="text-left px-4 py-2">Date</th></tr>
          </thead>
          <tbody>
            {stats.recentOrders.map((o, i) => (
              <tr key={i} className="border-t border-ink-100">
                <td className="px-4 py-2">{o.orderNumber}</td>
                <td className="px-4 py-2">{o.status}</td>
                <td className="px-4 py-2">{formatMoney(o.grandTotal)}</td>
                <td className="px-4 py-2 text-ink-400">{formatDate(o.createdAt)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
