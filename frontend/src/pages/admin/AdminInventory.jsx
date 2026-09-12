import React, { useEffect, useState } from 'react'
import { productApi } from '../../api/productApi'
import { inventoryApi } from '../../api/inventoryApi'
import { formatDateTime } from '../../utils/format'
import ErrorBanner from '../../components/ErrorBanner'
import EmptyState from '../../components/EmptyState'
import Spinner from '../../components/Spinner'

export default function AdminInventory() {
  const [lowStock, setLowStock] = useState(null)
  const [selected, setSelected] = useState(null)
  const [history, setHistory] = useState(null)
  const [adjustment, setAdjustment] = useState({ quantityChange: '', reason: '' })
  const [error, setError] = useState('')

  function load() { productApi.lowStock().then(setLowStock).catch(() => setLowStock([])) }
  useEffect(() => { load() }, [])

  function openProduct(p) {
    setSelected(p)
    setHistory(null)
    inventoryApi.history(p.id, 0, 20).then((h) => setHistory(h.content)).catch(() => setHistory([]))
  }

  async function submitAdjustment(e) {
    e.preventDefault()
    setError('')
    try {
      await inventoryApi.adjust(selected.id, { quantityChange: parseInt(adjustment.quantityChange, 10), reason: adjustment.reason })
      setAdjustment({ quantityChange: '', reason: '' })
      openProduct(selected)
      load()
    } catch (e) {
      setError(e.response?.data?.message || 'Could not adjust inventory.')
    }
  }

  if (!lowStock) return <Spinner label="Loading inventory" />

  return (
    <div className="grid md:grid-cols-[1fr_360px] gap-8">
      <div>
        <h2 className="font-medium text-ink-900 mb-1">Low stock products</h2>
        <p className="text-sm text-ink-400 mb-4">Products at or below their configured low-stock threshold.</p>
        {lowStock.length === 0 ? (
          <EmptyState title="No low-stock products" description="Everything is well-stocked right now." />
        ) : (
          <ul className="space-y-2">
            {lowStock.map((p) => (
              <li key={p.id}>
                <button onClick={() => openProduct(p)} className={`w-full flex justify-between rounded-md border p-3 text-sm text-left ${selected?.id === p.id ? 'border-brass-500 bg-brass-50' : 'border-ink-100'}`}>
                  <span className="font-medium text-ink-900">{p.name}</span>
                  <span className="text-red-600">{p.availableQuantity ?? (p.inventoryQuantity - (p.reservedQuantity || 0))} left</span>
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>

      {selected && (
        <div className="rounded-lg border border-ink-100 p-4 h-fit">
          <h3 className="font-medium text-ink-900 mb-3">{selected.name}</h3>
          <form onSubmit={submitAdjustment} className="space-y-3 mb-6">
            <ErrorBanner message={error} />
            <input required type="number" placeholder="Quantity change (+/-)" value={adjustment.quantityChange}
                   onChange={(e) => setAdjustment({ ...adjustment, quantityChange: e.target.value })}
                   className="w-full rounded border border-ink-100 px-3 py-2 text-sm" />
            <input required placeholder="Reason" value={adjustment.reason}
                   onChange={(e) => setAdjustment({ ...adjustment, reason: e.target.value })}
                   className="w-full rounded border border-ink-100 px-3 py-2 text-sm" />
            <button type="submit" className="rounded bg-ink-900 text-linen px-4 py-2 text-sm">Apply adjustment</button>
          </form>
          <h4 className="text-sm font-medium text-ink-900 mb-2">History</h4>
          {history === null ? <p className="text-sm text-ink-400">Loading…</p> : history.length === 0 ? <p className="text-sm text-ink-400">No history yet.</p> : (
            <ul className="space-y-2 text-xs text-ink-600">
              {history.map((h) => (
                <li key={h.id} className="flex justify-between">
                  <span>{h.reason} ({h.quantityChange > 0 ? '+' : ''}{h.quantityChange})</span>
                  <span className="text-ink-400">{formatDateTime(h.createdAt)}</span>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </div>
  )
}
