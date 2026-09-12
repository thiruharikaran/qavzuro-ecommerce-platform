import React, { useEffect, useMemo, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { productApi } from '../api/productApi'
import { categoryApi } from '../api/categoryApi'
import ProductCard from '../components/ProductCard'
import ProductCardSkeleton from '../components/ProductCardSkeleton'
import EmptyState from '../components/EmptyState'

const SORT_OPTIONS = [
  { value: 'relevance', label: 'Relevance' },
  { value: 'newest', label: 'Newest' },
  { value: 'price_asc', label: 'Price: Low to High' },
  { value: 'price_desc', label: 'Price: High to Low' },
  { value: 'rating', label: 'Highest Rated' },
  { value: 'popularity', label: 'Most Popular' },
]

export default function ProductList() {
  const [searchParams, setSearchParams] = useSearchParams()
  const [result, setResult] = useState(null)
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(true)

  const q = searchParams.get('q') || ''
  const categoryId = searchParams.get('categoryId') || ''
  const sort = searchParams.get('sort') || 'relevance'
  const minPrice = searchParams.get('minPrice') || ''
  const maxPrice = searchParams.get('maxPrice') || ''
  const inStockOnly = searchParams.get('inStockOnly') === 'true'
  const page = parseInt(searchParams.get('page') || '0', 10)

  useEffect(() => { categoryApi.listActive().then(setCategories).catch(() => {}) }, [])

  useEffect(() => {
    setLoading(true)
    const params = { keyword: q || undefined, categoryId: categoryId || undefined, sort, page, size: 12 }
    if (minPrice) params.minPrice = minPrice
    if (maxPrice) params.maxPrice = maxPrice
    if (inStockOnly) params.inStockOnly = true
    productApi.search(params)
      .then(setResult)
      .catch(() => setResult({ content: [], totalPages: 0, totalElements: 0 }))
      .finally(() => setLoading(false))
  }, [q, categoryId, sort, minPrice, maxPrice, inStockOnly, page])

  function updateParam(key, value) {
    const next = new URLSearchParams(searchParams)
    if (value) next.set(key, value); else next.delete(key)
    next.delete('page')
    setSearchParams(next)
  }

  const activeCategory = useMemo(() => categories.find((c) => c.id === categoryId), [categories, categoryId])

  return (
    <div className="mx-auto max-w-7xl px-4 py-10">
      <div className="mb-6">
        <h1 className="font-display text-3xl text-ink-900">
          {q ? `Results for "${q}"` : activeCategory ? activeCategory.name : 'All products'}
        </h1>
        {result && <p className="text-sm text-ink-400 mt-1">{result.totalElements} products</p>}
      </div>

      <div className="grid md:grid-cols-[220px_1fr] gap-8">
        <aside className="space-y-6">
          <div>
            <h3 className="text-sm font-semibold text-ink-900 mb-2">Category</h3>
            <ul className="space-y-1 text-sm text-ink-600">
              <li>
                <button onClick={() => updateParam('categoryId', '')} className={!categoryId ? 'font-semibold text-brass-600' : 'hover:text-brass-600'}>
                  All
                </button>
              </li>
              {categories.map((c) => (
                <li key={c.id}>
                  <button onClick={() => updateParam('categoryId', c.id)} className={categoryId === c.id ? 'font-semibold text-brass-600' : 'hover:text-brass-600'}>
                    {c.name}
                  </button>
                </li>
              ))}
            </ul>
          </div>

          <div>
            <h3 className="text-sm font-semibold text-ink-900 mb-2">Price</h3>
            <div className="flex gap-2">
              <input type="number" placeholder="Min" defaultValue={minPrice}
                     onBlur={(e) => updateParam('minPrice', e.target.value)}
                     className="w-full rounded border border-ink-100 px-2 py-1 text-sm" />
              <input type="number" placeholder="Max" defaultValue={maxPrice}
                     onBlur={(e) => updateParam('maxPrice', e.target.value)}
                     className="w-full rounded border border-ink-100 px-2 py-1 text-sm" />
            </div>
          </div>

          <label className="flex items-center gap-2 text-sm text-ink-600">
            <input type="checkbox" checked={inStockOnly} onChange={(e) => updateParam('inStockOnly', e.target.checked ? 'true' : '')} />
            In stock only
          </label>
        </aside>

        <div>
          <div className="flex justify-end mb-4">
            <select value={sort} onChange={(e) => updateParam('sort', e.target.value)}
                    aria-label="Sort products" className="rounded border border-ink-100 px-3 py-1.5 text-sm">
              {SORT_OPTIONS.map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
            </select>
          </div>

          {loading ? (
            <div className="grid grid-cols-2 lg:grid-cols-3 gap-6">
              {Array.from({ length: 9 }).map((_, i) => <ProductCardSkeleton key={i} />)}
            </div>
          ) : result?.content?.length ? (
            <>
              <div className="grid grid-cols-2 lg:grid-cols-3 gap-6">
                {result.content.map((p) => <ProductCard key={p.id} product={p} />)}
              </div>
              {result.totalPages > 1 && (
                <div className="mt-10 flex justify-center gap-2">
                  {Array.from({ length: result.totalPages }).map((_, i) => (
                    <button key={i} onClick={() => updateParam('page', String(i))}
                            className={`h-8 w-8 rounded text-sm ${i === page ? 'bg-ink-900 text-linen' : 'border border-ink-100 hover:border-brass-400'}`}>
                      {i + 1}
                    </button>
                  ))}
                </div>
              )}
            </>
          ) : (
            <EmptyState title="No products found" description="Try adjusting your filters or search terms." />
          )}
        </div>
      </div>
    </div>
  )
}
