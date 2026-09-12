import React, { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { productApi } from '../api/productApi'
import { categoryApi } from '../api/categoryApi'
import ProductCard from '../components/ProductCard'
import ProductCardSkeleton from '../components/ProductCardSkeleton'

export default function Home() {
  const [featured, setFeatured] = useState(null)
  const [categories, setCategories] = useState([])

  useEffect(() => {
    productApi.search({ sort: 'newest', size: 8 }).then((res) => setFeatured(res.content)).catch(() => setFeatured([]))
    categoryApi.listActive().then(setCategories).catch(() => setCategories([]))
  }, [])

  return (
    <div>
      <section className="bg-ink-900 text-linen">
        <div className="mx-auto max-w-7xl px-4 py-20 md:py-28 grid md:grid-cols-2 gap-10 items-center">
          <div>
            <p className="text-brass-400 text-sm font-medium mb-3">New season, considered picks</p>
            <h1 className="font-display text-4xl md:text-5xl leading-tight">
              Fewer things, chosen well.
            </h1>
            <p className="mt-5 text-ink-100 max-w-md">
              Qavzuro curates electronics, fashion, and home goods that earn a place in your life —
              backed by straightforward returns and real customer support.
            </p>
            <div className="mt-8 flex gap-3">
              <Link to="/products" className="rounded-md bg-brass-500 px-6 py-3 text-sm font-semibold text-ink-900 hover:bg-brass-400 transition-colors">
                Shop the collection
              </Link>
              <Link to="/products?sort=newest" className="rounded-md border border-ink-600 px-6 py-3 text-sm font-medium hover:border-brass-400 hover:text-brass-400 transition-colors">
                New arrivals
              </Link>
            </div>
          </div>
          <div className="hidden md:block">
            <img src="https://placehold.co/720x560?text=Qavzuro" alt="Featured Qavzuro products" className="rounded-lg shadow-2xl" />
          </div>
        </div>
      </section>

      {categories.length > 0 && (
        <section className="mx-auto max-w-7xl px-4 py-14">
          <h2 className="font-display text-2xl text-ink-900 mb-6">Shop by category</h2>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            {categories.map((c) => (
              <Link key={c.id} to={`/products?categoryId=${c.id}`} className="group relative aspect-[4/3] overflow-hidden rounded-lg bg-ink-50">
                <img src={c.imageUrl || `https://placehold.co/400x300?text=${encodeURIComponent(c.name)}`} alt={c.name}
                     loading="lazy" className="h-full w-full object-cover transition-transform group-hover:scale-105" />
                <div className="absolute inset-0 bg-gradient-to-t from-ink-900/70 to-transparent flex items-end p-4">
                  <span className="text-linen font-medium">{c.name}</span>
                </div>
              </Link>
            ))}
          </div>
        </section>
      )}

      <section className="mx-auto max-w-7xl px-4 py-14">
        <div className="flex items-center justify-between mb-6">
          <h2 className="font-display text-2xl text-ink-900">New arrivals</h2>
          <Link to="/products?sort=newest" className="text-sm text-brass-600 hover:text-brass-500">View all →</Link>
        </div>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
          {featured === null
            ? Array.from({ length: 8 }).map((_, i) => <ProductCardSkeleton key={i} />)
            : featured.length === 0
              ? <p className="col-span-full text-ink-400">No products yet — check back soon.</p>
              : featured.map((p) => <ProductCard key={p.id} product={p} />)}
        </div>
      </section>
    </div>
  )
}
