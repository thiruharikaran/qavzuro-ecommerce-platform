import React, { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { productApi } from '../../api/productApi'
import { formatMoney } from '../../utils/format'
import { useAuth } from '../../context/AuthContext'
import Spinner from '../../components/Spinner'
import EmptyState from '../../components/EmptyState'

export default function AdminProducts() {
  const { hasPermission } = useAuth()
  const [result, setResult] = useState(null)
  const [page, setPage] = useState(0)
  const [keyword, setKeyword] = useState('')
  const navigate = useNavigate()

  function load() {
    productApi.search({ keyword: keyword || undefined, page, size: 20, sort: 'newest' })
      .then(setResult).catch(() => setResult({ content: [] }))
  }
  useEffect(() => { load() }, [page])

  async function remove(id) {
    if (!confirm('Archive this product? It will no longer be visible to customers.')) return
    await productApi.remove(id)
    load()
  }

  if (!result) return <Spinner label="Loading products" />

  return (
    <div>
      <div className="flex justify-between items-center mb-4 gap-3">
        <form onSubmit={(e) => { e.preventDefault(); setPage(0); load() }} className="flex gap-2 flex-1 max-w-sm">
          <input placeholder="Search products…" value={keyword} onChange={(e) => setKeyword(e.target.value)}
                 className="w-full rounded border border-ink-100 px-3 py-2 text-sm" />
          <button type="submit" className="rounded border border-ink-900 px-3 text-sm">Search</button>
        </form>
        {hasPermission('PRODUCT_CREATE') && (
          <Link to="/admin/products/new" className="rounded-md bg-ink-900 px-4 py-2 text-sm font-medium text-linen whitespace-nowrap">+ New product</Link>
        )}
      </div>

      {result.content.length === 0 ? (
        <EmptyState title="No products found" />
      ) : (
        <div className="rounded-lg border border-ink-100 overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-ink-50 text-ink-400 text-xs uppercase">
              <tr>
                <th className="text-left px-4 py-2">Product</th>
                <th className="text-left px-4 py-2">SKU</th>
                <th className="text-left px-4 py-2">Price</th>
                <th className="text-left px-4 py-2">Stock</th>
                <th className="text-left px-4 py-2">Status</th>
                <th className="px-4 py-2"></th>
              </tr>
            </thead>
            <tbody>
              {result.content.map((p) => (
                <tr key={p.id} className="border-t border-ink-100">
                  <td className="px-4 py-2 font-medium text-ink-900">{p.name}</td>
                  <td className="px-4 py-2 text-ink-400">{p.sku}</td>
                  <td className="px-4 py-2">{formatMoney(p.salePrice || p.price)}</td>
                  <td className="px-4 py-2">{p.inventoryQuantity}</td>
                  <td className="px-4 py-2">{p.status}</td>
                  <td className="px-4 py-2 text-right whitespace-nowrap">
                    {hasPermission('PRODUCT_UPDATE') && <button onClick={() => navigate(`/admin/products/${p.id}/edit`)} className="text-brass-600 hover:text-brass-500 mr-3">Edit</button>}
                    {hasPermission('PRODUCT_DELETE') && <button onClick={() => remove(p.id)} className="text-ink-400 hover:text-red-600">Archive</button>}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

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
