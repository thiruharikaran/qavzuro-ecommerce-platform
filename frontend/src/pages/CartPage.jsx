import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useCart } from '../context/CartContext'
import { useAuth } from '../context/AuthContext'
import { formatMoney } from '../utils/format'
import EmptyState from '../components/EmptyState'
import Spinner from '../components/Spinner'
import ErrorBanner from '../components/ErrorBanner'

export default function CartPage() {
  const { cart, loading, updateItem, removeItem, applyCoupon, removeCoupon } = useCart()
  const { user } = useAuth()
  const navigate = useNavigate()
  const [couponInput, setCouponInput] = useState('')
  const [couponError, setCouponError] = useState('')

  if (!user) {
    return (
      <div className="mx-auto max-w-3xl px-4 py-20">
        <EmptyState title="Sign in to view your cart"
          description="Your cart is tied to your account so it's ready wherever you shop from."
          action={<Link to="/login" className="rounded-md bg-ink-900 px-6 py-3 text-sm font-medium text-linen">Sign in</Link>} />
      </div>
    )
  }

  if (loading && !cart) return <Spinner label="Loading cart" />

  const items = cart?.items || []

  async function handleApplyCoupon(e) {
    e.preventDefault()
    setCouponError('')
    try {
      await applyCoupon(couponInput)
      setCouponInput('')
    } catch (e) {
      setCouponError(e.response?.data?.message || 'Invalid coupon.')
    }
  }

  return (
    <div className="mx-auto max-w-5xl px-4 py-10">
      <h1 className="font-display text-3xl text-ink-900 mb-8">Your cart</h1>

      {items.length === 0 ? (
        <EmptyState title="Your cart is empty" description="Explore the catalog and find something you'll love."
          action={<Link to="/products" className="rounded-md bg-ink-900 px-6 py-3 text-sm font-medium text-linen">Continue shopping</Link>} />
      ) : (
        <div className="grid md:grid-cols-[1fr_320px] gap-10">
          <div className="space-y-4">
            {cart.warnings?.map((w, i) => <ErrorBanner key={i} message={w} />)}
            {items.map((item) => (
              <div key={item.productId + (item.variantId || '')} className="flex gap-4 border-b border-ink-100 pb-4">
                <img src={item.imageUrl || 'https://placehold.co/120x120'} alt={item.name} className="h-24 w-24 rounded object-cover bg-ink-50" />
                <div className="flex-1">
                  <p className="font-medium text-ink-900">{item.name}</p>
                  {!item.available && <p className="text-xs text-red-600 mt-1">Limited availability — only {item.availableQuantity} left</p>}
                  <p className="text-sm text-ink-400 mt-1">{formatMoney(item.unitPrice)}</p>
                  <div className="mt-2 flex items-center gap-3">
                    <input type="number" min={0} value={item.quantity}
                           onChange={(e) => updateItem(item.productId, item.variantId, Math.max(0, parseInt(e.target.value || '0', 10)))}
                           className="w-16 rounded border border-ink-100 px-2 py-1 text-sm" />
                    <button onClick={() => removeItem(item.productId, item.variantId)} className="text-xs text-ink-400 hover:text-red-600">
                      Remove
                    </button>
                  </div>
                </div>
                <div className="font-medium text-ink-900">{formatMoney(item.lineTotal)}</div>
              </div>
            ))}
          </div>

          <div className="rounded-lg border border-ink-100 p-6 h-fit">
            <h2 className="font-medium text-ink-900 mb-4">Order summary</h2>
            <div className="space-y-2 text-sm">
              <div className="flex justify-between"><span className="text-ink-400">Subtotal</span><span>{formatMoney(cart.subtotal)}</span></div>
              {cart.discountTotal > 0 && (
                <div className="flex justify-between text-green-700"><span>Discount</span><span>-{formatMoney(cart.discountTotal)}</span></div>
              )}
              <div className="flex justify-between text-ink-400"><span>Shipping & tax</span><span>Calculated at checkout</span></div>
              <div className="flex justify-between font-semibold text-ink-900 pt-2 border-t border-ink-100">
                <span>Estimated total</span><span>{formatMoney(cart.estimatedTotal)}</span>
              </div>
            </div>

            <div className="mt-4">
              {cart.appliedCouponCode ? (
                <div className="flex items-center justify-between text-sm bg-brass-50 rounded px-3 py-2">
                  <span>Coupon <strong>{cart.appliedCouponCode}</strong> applied</span>
                  <button onClick={removeCoupon} className="text-ink-400 hover:text-red-600">Remove</button>
                </div>
              ) : (
                <form onSubmit={handleApplyCoupon} className="flex gap-2">
                  <input placeholder="Coupon code" value={couponInput} onChange={(e) => setCouponInput(e.target.value)}
                         className="flex-1 rounded border border-ink-100 px-3 py-2 text-sm" />
                  <button type="submit" className="rounded border border-ink-900 px-4 text-sm font-medium">Apply</button>
                </form>
              )}
              <ErrorBanner message={couponError} />
            </div>

            <button onClick={() => navigate('/checkout')} disabled={items.some((i) => !i.available)}
                    className="mt-6 w-full rounded-md bg-brass-500 px-6 py-3 text-sm font-semibold text-ink-900 hover:bg-brass-400 disabled:opacity-50 transition-colors">
              Proceed to checkout
            </button>
          </div>
        </div>
      )}
    </div>
  )
}
