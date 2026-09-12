import React, { useEffect, useState } from 'react'
import { categoryApi } from '../../api/categoryApi'
import ErrorBanner from '../../components/ErrorBanner'

const EMPTY = { name: '', slug: '', description: '', imageUrl: '', parentId: '' }

export default function AdminCategories() {
  const [categories, setCategories] = useState(null)
  const [form, setForm] = useState(EMPTY)
  const [editingId, setEditingId] = useState(null)
  const [error, setError] = useState('')

  function load() { categoryApi.listAll().then(setCategories).catch(() => setCategories([])) }
  useEffect(() => { load() }, [])

  async function submit(e) {
    e.preventDefault()
    setError('')
    try {
      if (editingId) await categoryApi.update(editingId, form)
      else await categoryApi.create(form)
      setForm(EMPTY)
      setEditingId(null)
      load()
    } catch (e) {
      setError(e.response?.data?.message || 'Could not save category.')
    }
  }

  function edit(c) {
    setForm({ name: c.name, slug: c.slug, description: c.description || '', imageUrl: c.imageUrl || '', parentId: c.parentId || '' })
    setEditingId(c.id)
  }

  async function toggleActive(c) {
    await categoryApi.setActive(c.id, !c.active)
    load()
  }

  if (!categories) return null

  return (
    <div className="grid md:grid-cols-[1fr_320px] gap-8">
      <div>
        <h2 className="font-medium text-ink-900 mb-4">Categories</h2>
        <div className="rounded-lg border border-ink-100 overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-ink-50 text-ink-400 text-xs uppercase">
              <tr><th className="text-left px-4 py-2">Name</th><th className="text-left px-4 py-2">Slug</th><th className="text-left px-4 py-2">Active</th><th className="px-4 py-2"></th></tr>
            </thead>
            <tbody>
              {categories.map((c) => (
                <tr key={c.id} className="border-t border-ink-100">
                  <td className="px-4 py-2 font-medium text-ink-900">{c.name}</td>
                  <td className="px-4 py-2 text-ink-400">{c.slug}</td>
                  <td className="px-4 py-2">{c.active ? 'Yes' : 'No'}</td>
                  <td className="px-4 py-2 text-right whitespace-nowrap">
                    <button onClick={() => edit(c)} className="text-brass-600 hover:text-brass-500 mr-3">Edit</button>
                    <button onClick={() => toggleActive(c)} className="text-ink-400 hover:text-ink-900">{c.active ? 'Deactivate' : 'Activate'}</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      <form onSubmit={submit} className="space-y-3 rounded-lg border border-ink-100 p-4 h-fit">
        <h3 className="font-medium text-ink-900">{editingId ? 'Edit category' : 'New category'}</h3>
        <ErrorBanner message={error} />
        <input required placeholder="Name" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} className="w-full rounded border border-ink-100 px-3 py-2 text-sm" />
        <input required placeholder="Slug" value={form.slug} onChange={(e) => setForm({ ...form, slug: e.target.value })} className="w-full rounded border border-ink-100 px-3 py-2 text-sm" />
        <textarea placeholder="Description" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} className="w-full rounded border border-ink-100 px-3 py-2 text-sm" rows={2} />
        <input placeholder="Image URL" value={form.imageUrl} onChange={(e) => setForm({ ...form, imageUrl: e.target.value })} className="w-full rounded border border-ink-100 px-3 py-2 text-sm" />
        <select value={form.parentId} onChange={(e) => setForm({ ...form, parentId: e.target.value })} className="w-full rounded border border-ink-100 px-3 py-2 text-sm">
          <option value="">No parent (top-level)</option>
          {categories.filter((c) => c.id !== editingId).map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
        <div className="flex gap-2">
          <button type="submit" className="rounded bg-ink-900 text-linen px-4 py-2 text-sm">{editingId ? 'Save' : 'Create'}</button>
          {editingId && <button type="button" onClick={() => { setEditingId(null); setForm(EMPTY) }} className="rounded border border-ink-100 px-4 py-2 text-sm">Cancel</button>}
        </div>
      </form>
    </div>
  )
}
