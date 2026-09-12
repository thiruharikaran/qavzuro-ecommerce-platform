import React, { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { wishlistApi } from '../api/wishlistApi'
import { useCart } from '../context/CartContext'
import EmptyState from '../components/EmptyState'
import Spinner from '../components/Spinner'

export default function WishlistPage() {
  const [wishlist, setWishlist] = useState(null)
  const { refresh } = useCart()

  function load() { wishlistApi.get().then(setWishlist).catch(() => setWishlist({ items: [] })) }
  useEffect(() => { load() }, [])

  if (!wishlist) return <Spinner label="Loading wishlist" />

  async function remove(productId) {
    await wishlistApi.remove(productId)
    load()
  }
  async function moveToCart(productId) {
    await wishlistApi.moveToCart(productId)
    await refresh()
    load()
  }

  return (
    <div className="mx-auto max-w-4xl px-4 py-10">
      <h1 className="font-display text-3xl text-ink-900 mb-8">Your wishlist</h1>
      {wishlist.items.length === 0 ? (
        <EmptyState title="Your wishlist is empty" description="Save items you love for later."
          action={<Link to="/products" className="rounded-md bg-ink-900 px-6 py-3 text-sm font-medium text-linen">Browse products</Link>} />
      ) : (
        <ul className="space-y-4">
          {wishlist.items.map((item) => (
            <li key={item.productId} className="flex items-center gap-4 border-b border-ink-100 pb-4">
              <img src={item.imageUrlSnapshot || 'https://placehold.co/100x100'} alt={item.productNameSnapshot} className="h-20 w-20 rounded object-cover bg-ink-50" />
              <span className="flex-1 font-medium text-ink-900">{item.productNameSnapshot}</span>
              <button onClick={() => moveToCart(item.productId)} className="rounded border border-ink-900 px-4 py-2 text-sm">Move to cart</button>
              <button onClick={() => remove(item.productId)} className="text-sm text-ink-400 hover:text-red-600">Remove</button>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
