import React, { useEffect, useState } from 'react'
import { userApi } from '../../api/userApi'
import ErrorBanner from '../../components/ErrorBanner'
import EmptyState from '../../components/EmptyState'

const EMPTY = { label: '', fullName: '', phone: '', line1: '', line2: '', city: '', state: '', postalCode: '', country: 'India', defaultShipping: false, defaultBilling: false }

export default function AccountAddresses() {
  const [addresses, setAddresses] = useState(null)
  const [form, setForm] = useState(EMPTY)
  const [editingId, setEditingId] = useState(null)
  const [showForm, setShowForm] = useState(false)
  const [error, setError] = useState('')

  function load() { userApi.addresses().then(setAddresses).catch(() => setAddresses([])) }
  useEffect(() => { load() }, [])

  function startEdit(address) {
    setForm(address)
    setEditingId(address.id)
    setShowForm(true)
  }

  function startNew() {
    setForm(EMPTY)
    setEditingId(null)
    setShowForm(true)
  }

  async function submit(e) {
    e.preventDefault()
    setError('')
    try {
      const updated = await userApi.upsertAddress(editingId ? { ...form, id: editingId } : form)
      setAddresses(updated)
      setShowForm(false)
      setForm(EMPTY)
      setEditingId(null)
    } catch (e) {
      setError(e.response?.data?.message || 'Could not save address.')
    }
  }

  async function remove(id) {
    const updated = await userApi.deleteAddress(id)
    setAddresses(updated)
  }

  if (!addresses) return null

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <h2 className="font-medium text-ink-900">Saved addresses</h2>
        <button onClick={startNew} className="text-sm text-brass-600 hover:text-brass-500 font-medium">+ Add address</button>
      </div>

      {addresses.length === 0 && !showForm ? (
        <EmptyState title="No addresses yet" description="Add a shipping address to speed up checkout." />
      ) : (
        <ul className="space-y-3 mb-6">
          {addresses.map((a) => (
            <li key={a.id} className="rounded-md border border-ink-100 p-4 text-sm">
              <div className="flex justify-between">
                <div>
                  <p className="font-medium text-ink-900">{a.label || 'Address'} {a.defaultShipping && <span className="text-xs text-brass-600 ml-1">(default shipping)</span>} {a.defaultBilling && <span className="text-xs text-brass-600 ml-1">(default billing)</span>}</p>
                  <p className="text-ink-600 mt-1">{a.fullName} · {a.phone}</p>
                  <p className="text-ink-600">{a.line1}{a.line2 ? `, ${a.line2}` : ''}, {a.city}, {a.state} {a.postalCode}, {a.country}</p>
                </div>
                <div className="space-x-3 shrink-0">
                  <button onClick={() => startEdit(a)} className="text-brass-600 hover:text-brass-500">Edit</button>
                  <button onClick={() => remove(a.id)} className="text-ink-400 hover:text-red-600">Delete</button>
                </div>
              </div>
            </li>
          ))}
        </ul>
      )}

      {showForm && (
        <form onSubmit={submit} className="rounded-md border border-ink-100 p-4 grid grid-cols-2 gap-3 max-w-xl">
          <ErrorBanner message={error} />
          <input placeholder="Label (e.g. Home)" value={form.label} onChange={(e) => setForm({ ...form, label: e.target.value })} className="col-span-2 rounded border border-ink-100 px-3 py-2 text-sm" />
          <input required placeholder="Full name" value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} className="rounded border border-ink-100 px-3 py-2 text-sm" />
          <input required placeholder="Phone" value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} className="rounded border border-ink-100 px-3 py-2 text-sm" />
          <input required placeholder="Address line 1" value={form.line1} onChange={(e) => setForm({ ...form, line1: e.target.value })} className="col-span-2 rounded border border-ink-100 px-3 py-2 text-sm" />
          <input placeholder="Address line 2" value={form.line2 || ''} onChange={(e) => setForm({ ...form, line2: e.target.value })} className="col-span-2 rounded border border-ink-100 px-3 py-2 text-sm" />
          <input required placeholder="City" value={form.city} onChange={(e) => setForm({ ...form, city: e.target.value })} className="rounded border border-ink-100 px-3 py-2 text-sm" />
          <input required placeholder="State" value={form.state} onChange={(e) => setForm({ ...form, state: e.target.value })} className="rounded border border-ink-100 px-3 py-2 text-sm" />
          <input required placeholder="Postal code" value={form.postalCode} onChange={(e) => setForm({ ...form, postalCode: e.target.value })} className="rounded border border-ink-100 px-3 py-2 text-sm" />
          <input required placeholder="Country" value={form.country} onChange={(e) => setForm({ ...form, country: e.target.value })} className="rounded border border-ink-100 px-3 py-2 text-sm" />
          <label className="flex items-center gap-2 text-sm"><input type="checkbox" checked={form.defaultShipping} onChange={(e) => setForm({ ...form, defaultShipping: e.target.checked })} /> Default shipping</label>
          <label className="flex items-center gap-2 text-sm"><input type="checkbox" checked={form.defaultBilling} onChange={(e) => setForm({ ...form, defaultBilling: e.target.checked })} /> Default billing</label>
          <div className="col-span-2 flex gap-3">
            <button type="submit" className="rounded bg-ink-900 text-linen px-5 py-2 text-sm">Save address</button>
            <button type="button" onClick={() => setShowForm(false)} className="rounded border border-ink-100 px-5 py-2 text-sm">Cancel</button>
          </div>
        </form>
      )}
    </div>
  )
}
