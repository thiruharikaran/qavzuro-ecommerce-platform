import React from 'react'
import { Link } from 'react-router-dom'
import { formatMoney } from '../utils/format'

export default function ProductCard({ product }) {
  const image = product.images?.[0]?.url || 'https://placehold.co/600x600?text=Qavzuro'
  const hasSale = product.salePrice && product.salePrice < product.price
  const outOfStock = (product.inventoryQuantity ?? 0) <= 0 && (!product.variants || product.variants.every(v => v.stockQuantity <= 0))

  return (
    <Link to={`/products/${product.slug}`} className="group block">
      <div className="relative aspect-square overflow-hidden rounded-md bg-ink-50">
        <img
          src={image}
          alt={product.images?.[0]?.altText || product.name}
          loading="lazy"
          className="h-full w-full object-cover transition-transform duration-300 group-hover:scale-105"
        />
        {hasSale && (
          <span className="absolute left-2 top-2 rounded bg-brass-500 px-2 py-0.5 text-xs font-semibold text-ink-900">Sale</span>
        )}
        {outOfStock && (
          <span className="absolute right-2 top-2 rounded bg-ink-800/80 px-2 py-0.5 text-xs font-medium text-linen">Out of stock</span>
        )}
      </div>
      <div className="mt-3 space-y-1">
        <p className="text-xs uppercase tracking-wide text-ink-400">{product.brand}</p>
        <h3 className="text-sm font-medium text-ink-900 line-clamp-2">{product.name}</h3>
        <div className="flex items-baseline gap-2">
          <span className="font-semibold text-ink-900">{formatMoney(hasSale ? product.salePrice : product.price, product.currency)}</span>
          {hasSale && <span className="text-sm text-ink-400 line-through">{formatMoney(product.price, product.currency)}</span>}
        </div>
        {product.ratingSummary?.count > 0 && (
          <p className="text-xs text-ink-400">★ {product.ratingSummary.average.toFixed(1)} ({product.ratingSummary.count})</p>
        )}
      </div>
    </Link>
  )
}
