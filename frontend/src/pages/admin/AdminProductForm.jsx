import React, { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { productApi } from '../../api/productApi'
import { categoryApi } from '../../api/categoryApi'
import ErrorBanner from '../../components/ErrorBanner'
import Spinner from '../../components/Spinner'

const EMPTY = {
  sku: '', name: '', description: '', shortDescription: '', brand: '', categoryId: '',
  price: '', salePrice: '', taxRatePercent: '0', inventoryQuantity: '0', lowStockThreshold: '5',
  tags: '', imageUrl: '',
}

export default function AdminProductForm() {
  const { id } = useParams()
  const isEdit = Boolean(id)
  const navigate = useNavigate()
  const [categories, setCategories] = useState([])
  const [form, setForm] = useState(EMPTY)
  const [status, setStatus] = useState('DRAFT')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(isEdit)
  const [saving, setSaving] = useState(false)

  useEffect(() => { categoryApi.listActive().then(setCategories).catch(() => setCategories([])) }, [])

  useEffect(() => {
    if (!isEdit) return
    productApi.getById(id).then((p) => {
      setForm({
        sku: p.sku, name: p.name, description: p.description || '', shortDescription: p.shortDescription || '',
        brand: p.brand || '', categoryId: p.categoryId || '',
        price: p.price, salePrice: p.salePrice || '', taxRatePercent: p.taxRatePercent,
        inventoryQuantity: p.inventoryQuantity, lowStockThreshold: p.lowStockThreshold,
        tags: (p.tags || []).join(', '), imageUrl: p.images?.[0]?.url || '',
      })
      setStatus(p.status)
      setLoading(false)
    }).catch(() => setLoading(false))
  }, [id, isEdit])

  async function submit(e) {
    e.preventDefault()
    setError('')
    setSaving(true)
    try {
      const payload = {
        name: form.name,
        description: form.description,
        shortDescription: form.shortDescription,
        brand: form.brand,
        categoryId: form.categoryId,
        price: parseFloat(form.price),
        salePrice: form.salePrice ? parseFloat(form.salePrice) : null,
        taxRatePercent: parseFloat(form.taxRatePercent || '0'),
        tags: form.tags.split(',').map((t) => t.trim()).filter(Boolean),
      }
      if (isEdit) {
        payload.status = status
        payload.lowStockThreshold = parseInt(form.lowStockThreshold || '5', 10)
        await productApi.update(id, payload)
        navigate('/admin/products')
      } else {
        const createPayload = {
          sku: form.sku, ...payload,
          inventoryQuantity: parseInt(form.inventoryQuantity || '0', 10),
          lowStockThreshold: parseInt(form.lowStockThreshold || '5', 10),
          images: form.imageUrl ? [{ url: form.imageUrl, altText: form.name }] : [],
        }
        const created = await productApi.create(createPayload)
        navigate(`/admin/products/${created.id}/edit`)
      }
    } catch (e) {
      setError(e.response?.data?.message || 'Could not save product.')
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <Spinner label="Loading product" />

  return (
    <div className="max-w-2xl">
      <h2 className="font-medium text-ink-900 mb-4">{isEdit ? 'Edit product' : 'New product'}</h2>
      <form onSubmit={submit} className="space-y-4">
        <ErrorBanner message={error} />
        <div className="grid grid-cols-2 gap-3">
          {!isEdit && (
            <div>
              <label className="text-sm font-medium text-ink-900">SKU</label>
              <input required value={form.sku} onChange={(e) => setForm({ ...form, sku: e.target.value })} className="mt-1 w-full rounded border border-ink-100 px-3 py-2 text-sm" />
            </div>
          )}
          <div>
            <label className="text-sm font-medium text-ink-900">Name</label>
            <input required value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} className="mt-1 w-full rounded border border-ink-100 px-3 py-2 text-sm" />
          </div>
        </div>
        <div>
          <label className="text-sm font-medium text-ink-900">Short description</label>
          <input value={form.shortDescription} onChange={(e) => setForm({ ...form, shortDescription: e.target.value })} className="mt-1 w-full rounded border border-ink-100 px-3 py-2 text-sm" />
        </div>
        <div>
          <label className="text-sm font-medium text-ink-900">Description</label>
          <textarea rows={4} value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} className="mt-1 w-full rounded border border-ink-100 px-3 py-2 text-sm" />
        </div>
        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="text-sm font-medium text-ink-900">Brand</label>
            <input value={form.brand} onChange={(e) => setForm({ ...form, brand: e.target.value })} className="mt-1 w-full rounded border border-ink-100 px-3 py-2 text-sm" />
          </div>
          <div>
            <label className="text-sm font-medium text-ink-900">Category</label>
            <select required value={form.categoryId} onChange={(e) => setForm({ ...form, categoryId: e.target.value })} className="mt-1 w-full rounded border border-ink-100 px-3 py-2 text-sm">
              <option value="">Select…</option>
              {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
          </div>
        </div>
        <div className="grid grid-cols-3 gap-3">
          <div>
            <label className="text-sm font-medium text-ink-900">Price</label>
            <input required type="number" step="0.01" value={form.price} onChange={(e) => setForm({ ...form, price: e.target.value })} className="mt-1 w-full rounded border border-ink-100 px-3 py-2 text-sm" />
          </div>
          <div>
            <label className="text-sm font-medium text-ink-900">Sale price</label>
            <input type="number" step="0.01" value={form.salePrice} onChange={(e) => setForm({ ...form, salePrice: e.target.value })} className="mt-1 w-full rounded border border-ink-100 px-3 py-2 text-sm" />
          </div>
          <div>
            <label className="text-sm font-medium text-ink-900">Tax %</label>
            <input type="number" step="0.01" value={form.taxRatePercent} onChange={(e) => setForm({ ...form, taxRatePercent: e.target.value })} className="mt-1 w-full rounded border border-ink-100 px-3 py-2 text-sm" />
          </div>
        </div>
        {!isEdit && (
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="text-sm font-medium text-ink-900">Initial stock</label>
              <input type="number" value={form.inventoryQuantity} onChange={(e) => setForm({ ...form, inventoryQuantity: e.target.value })} className="mt-1 w-full rounded border border-ink-100 px-3 py-2 text-sm" />
            </div>
            <div>
              <label className="text-sm font-medium text-ink-900">Low stock threshold</label>
              <input type="number" value={form.lowStockThreshold} onChange={(e) => setForm({ ...form, lowStockThreshold: e.target.value })} className="mt-1 w-full rounded border border-ink-100 px-3 py-2 text-sm" />
            </div>
          </div>
        )}
        <div>
          <label className="text-sm font-medium text-ink-900">Image URL</label>
          <input value={form.imageUrl} onChange={(e) => setForm({ ...form, imageUrl: e.target.value })} placeholder="https://…" className="mt-1 w-full rounded border border-ink-100 px-3 py-2 text-sm" />
        </div>
        <div>
          <label className="text-sm font-medium text-ink-900">Tags (comma separated)</label>
          <input value={form.tags} onChange={(e) => setForm({ ...form, tags: e.target.value })} className="mt-1 w-full rounded border border-ink-100 px-3 py-2 text-sm" />
        </div>
        {isEdit && (
          <div>
            <label className="text-sm font-medium text-ink-900">Status</label>
            <select value={status} onChange={(e) => setStatus(e.target.value)} className="mt-1 w-full rounded border border-ink-100 px-3 py-2 text-sm">
              {['DRAFT', 'ACTIVE', 'INACTIVE', 'ARCHIVED'].map((s) => <option key={s} value={s}>{s}</option>)}
            </select>
          </div>
        )}
        <button type="submit" disabled={saving} className="rounded-md bg-ink-900 px-6 py-2.5 text-sm font-medium text-linen hover:bg-ink-800 disabled:opacity-50">
          {saving ? 'Saving…' : isEdit ? 'Save changes' : 'Create product'}
        </button>
      </form>
    </div>
  )
}
