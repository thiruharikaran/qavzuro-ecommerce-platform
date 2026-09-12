import React, { useEffect, useState } from 'react'
import { couponApi } from '../../api/couponApi'
import { formatDate } from '../../utils/format'
import ErrorBanner from '../../components/ErrorBanner'

const EMPTY = { code: '', discountType: 'PERCENTAGE', discountValue: '', minimumOrderValue: '', maximumDiscountAmount: '', expirationDate: '', usageLimit: '', perUserLimit: '1' }

export default function AdminCoupons() {
  const [coupons, setCoupons] = useState(null)
  const [form, setForm] = useState(EMPTY)
  const [error, setError] = useState('')

  function load() { couponApi.list().then(setCoupons).catch(() => setCoupons([])) }
  useEffect(() => { load() }, [])

  async function submit(e) {
    e.preventDefault()
    setError('')
    try {
      await couponApi.create({
        code: form.code,
        discountType: form.discountType,
        discountValue: parseFloat(form.discountValue),
        minimumOrderValue: form.minimumOrderValue ? parseFloat(form.minimumOrderValue) : null,
        maximumDiscountAmount: form.maximumDiscountAmount ? parseFloat(form.maximumDiscountAmount) : null,
        expirationDate: form.expirationDate ? new Date(form.expirationDate).toISOString() : null,
        usageLimit: form.usageLimit ? parseInt(form.usageLimit, 10) : null,
        perUserLimit: form.perUserLimit ? parseInt(form.perUserLimit, 10) : null,
      })
      setForm(EMPTY)
      load()
    } catch (e) {
      setError(e.response?.data?.message || 'Could not create coupon.')
    }
  }

  async function toggleActive(c) {
    await couponApi.setActive(c.id, !c.active)
    load()
  }

  if (!coupons) return null

  return (
    <div className="grid md:grid-cols-[1fr_320px] gap-8">
      <div>
        <h2 className="font-medium text-ink-900 mb-4">Coupons</h2>
        <div className="rounded-lg border border-ink-100 overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-ink-50 text-ink-400 text-xs uppercase">
              <tr><th className="text-left px-4 py-2">Code</th><th className="text-left px-4 py-2">Discount</th><th className="text-left px-4 py-2">Used</th><th className="text-left px-4 py-2">Expires</th><th className="text-left px-4 py-2">Active</th><th></th></tr>
            </thead>
            <tbody>
              {coupons.map((c) => (
                <tr key={c.id} className="border-t border-ink-100">
                  <td className="px-4 py-2 font-medium text-ink-900">{c.code}</td>
                  <td className="px-4 py-2">{c.discountType === 'PERCENTAGE' ? `${c.discountValue}%` : `₹${c.discountValue}`}</td>
                  <td className="px-4 py-2">{c.timesUsed}{c.usageLimit ? ` / ${c.usageLimit}` : ''}</td>
                  <td className="px-4 py-2 text-ink-400">{c.expirationDate ? formatDate(c.expirationDate) : '—'}</td>
                  <td className="px-4 py-2">{c.active ? 'Yes' : 'No'}</td>
                  <td className="px-4 py-2 text-right"><button onClick={() => toggleActive(c)} className="text-brass-600 hover:text-brass-500">{c.active ? 'Deactivate' : 'Activate'}</button></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      <form onSubmit={submit} className="space-y-3 rounded-lg border border-ink-100 p-4 h-fit">
        <h3 className="font-medium text-ink-900">New coupon</h3>
        <ErrorBanner message={error} />
        <input required placeholder="Code (e.g. SAVE20)" value={form.code} onChange={(e) => setForm({ ...form, code: e.target.value.toUpperCase() })} className="w-full rounded border border-ink-100 px-3 py-2 text-sm" />
        <select value={form.discountType} onChange={(e) => setForm({ ...form, discountType: e.target.value })} className="w-full rounded border border-ink-100 px-3 py-2 text-sm">
          <option value="PERCENTAGE">Percentage</option>
          <option value="FIXED">Fixed amount</option>
        </select>
        <input required type="number" step="0.01" placeholder="Discount value" value={form.discountValue} onChange={(e) => setForm({ ...form, discountValue: e.target.value })} className="w-full rounded border border-ink-100 px-3 py-2 text-sm" />
        <input type="number" step="0.01" placeholder="Minimum order value" value={form.minimumOrderValue} onChange={(e) => setForm({ ...form, minimumOrderValue: e.target.value })} className="w-full rounded border border-ink-100 px-3 py-2 text-sm" />
        <input type="number" step="0.01" placeholder="Max discount amount" value={form.maximumDiscountAmount} onChange={(e) => setForm({ ...form, maximumDiscountAmount: e.target.value })} className="w-full rounded border border-ink-100 px-3 py-2 text-sm" />
        <input type="date" placeholder="Expiration date" value={form.expirationDate} onChange={(e) => setForm({ ...form, expirationDate: e.target.value })} className="w-full rounded border border-ink-100 px-3 py-2 text-sm" />
        <input type="number" placeholder="Total usage limit" value={form.usageLimit} onChange={(e) => setForm({ ...form, usageLimit: e.target.value })} className="w-full rounded border border-ink-100 px-3 py-2 text-sm" />
        <input type="number" placeholder="Per-user limit" value={form.perUserLimit} onChange={(e) => setForm({ ...form, perUserLimit: e.target.value })} className="w-full rounded border border-ink-100 px-3 py-2 text-sm" />
        <button type="submit" className="rounded bg-ink-900 text-linen px-4 py-2 text-sm">Create coupon</button>
      </form>
    </div>
  )
}
