import React, { useEffect, useState } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { productApi } from '../api/productApi'
import { reviewApi } from '../api/reviewApi'
import { wishlistApi } from '../api/wishlistApi'
import { useAuth } from '../context/AuthContext'
import { useCart } from '../context/CartContext'
import { formatMoney, formatDate } from '../utils/format'
import ProductCard from '../components/ProductCard'
import ErrorBanner from '../components/ErrorBanner'
import Spinner from '../components/Spinner'

export default function ProductDetail() {
  const { slug } = useParams()
  const navigate = useNavigate()
  const { user } = useAuth()
  const { addItem } = useCart()

  const [product, setProduct] = useState(null)
  const [related, setRelated] = useState([])
  const [reviews, setReviews] = useState(null)
  const [selectedVariant, setSelectedVariant] = useState(null)
  const [quantity, setQuantity] = useState(1)
  const [activeImage, setActiveImage] = useState(0)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const [reviewForm, setReviewForm] = useState({ rating: 5, title: '', reviewText: '' })
  const [reviewError, setReviewError] = useState('')
  const [reviewSuccess, setReviewSuccess] = useState('')

  useEffect(() => {
    setProduct(null)
    productApi.getBySlug(slug).then((p) => {
      setProduct(p)
      setActiveImage(0)
      if (p.variants?.length) setSelectedVariant(p.variants[0].variantId)
      productApi.related(p.id).then(setRelated).catch(() => setRelated([]))
      reviewApi.forProduct(p.id).then(setReviews).catch(() => setReviews({ content: [] }))
    }).catch(() => setProduct(false))
  }, [slug])

  if (product === null) return <Spinner label="Loading product" />
  if (product === false) return <div className="mx-auto max-w-3xl px-4 py-20 text-center text-ink-400">Product not found.</div>

  const variant = product.variants?.find((v) => v.variantId === selectedVariant)
  const price = variant?.priceOverride ?? (product.salePrice && product.salePrice < product.price ? product.salePrice : product.price)
  const available = variant ? variant.stockQuantity - variant.reservedQuantity : product.inventoryQuantity - (product.reservedQuantity || 0)
  const images = product.images?.length ? product.images : [{ url: 'https://placehold.co/700x700?text=Qavzuro' }]

  async function handleAddToCart() {
    if (!user) { navigate('/login'); return }
    setError('')
    setBusy(true)
    try {
      await addItem(product.id, selectedVariant, quantity)
    } catch (e) {
      setError(e.response?.data?.message || 'Could not add item to cart.')
    } finally {
      setBusy(false)
    }
  }

  async function handleBuyNow() {
    await handleAddToCart()
    if (user) navigate('/checkout')
  }

  async function handleWishlist() {
    if (!user) { navigate('/login'); return }
    try { await wishlistApi.add(product.id) } catch { /* likely already in wishlist */ }
  }

  async function submitReview(e) {
    e.preventDefault()
    setReviewError('')
    setReviewSuccess('')
    try {
      await reviewApi.create(product.id, reviewForm)
      setReviewSuccess('Thanks — your review has been submitted for moderation.')
      setReviewForm({ rating: 5, title: '', reviewText: '' })
    } catch (e) {
      setReviewError(e.response?.data?.message || 'Could not submit review.')
    }
  }

  return (
    <div className="mx-auto max-w-7xl px-4 py-10">
      <nav className="text-xs text-ink-400 mb-6">
        <Link to="/products" className="hover:text-brass-600">Shop</Link> / <span>{product.name}</span>
      </nav>

      <div className="grid md:grid-cols-2 gap-10">
        <div>
          <div className="aspect-square overflow-hidden rounded-lg bg-ink-50">
            <img src={images[activeImage]?.url} alt={images[activeImage]?.altText || product.name} className="h-full w-full object-cover" />
          </div>
          {images.length > 1 && (
            <div className="mt-3 flex gap-2">
              {images.map((img, i) => (
                <button key={i} onClick={() => setActiveImage(i)}
                        className={`h-16 w-16 overflow-hidden rounded border-2 ${i === activeImage ? 'border-brass-500' : 'border-transparent'}`}>
                  <img src={img.url} alt="" loading="lazy" className="h-full w-full object-cover" />
                </button>
              ))}
            </div>
          )}
        </div>

        <div>
          <p className="text-xs uppercase tracking-wide text-ink-400">{product.brand}</p>
          <h1 className="font-display text-3xl text-ink-900 mt-1">{product.name}</h1>
          {product.ratingSummary?.count > 0 && (
            <p className="mt-2 text-sm text-ink-600">★ {product.ratingSummary.average.toFixed(1)} · {product.ratingSummary.count} reviews</p>
          )}

          <div className="mt-4 flex items-baseline gap-3">
            <span className="text-2xl font-semibold text-ink-900">{formatMoney(price, product.currency)}</span>
            {product.salePrice && product.salePrice < product.price && !variant && (
              <span className="text-ink-400 line-through">{formatMoney(product.price, product.currency)}</span>
            )}
          </div>

          <p className="mt-4 text-ink-600">{product.shortDescription}</p>

          {product.variants?.length > 0 && (
            <div className="mt-6">
              <h3 className="text-sm font-medium text-ink-900 mb-2">Options</h3>
              <div className="flex flex-wrap gap-2">
                {product.variants.map((v) => (
                  <button key={v.variantId} onClick={() => setSelectedVariant(v.variantId)}
                          disabled={v.stockQuantity - v.reservedQuantity <= 0}
                          className={`rounded-md border px-3 py-1.5 text-sm ${selectedVariant === v.variantId ? 'border-brass-500 bg-brass-50 text-ink-900' : 'border-ink-100 text-ink-600'} disabled:opacity-40 disabled:cursor-not-allowed`}>
                    {Object.values(v.attributes || {}).join(' / ')}
                  </button>
                ))}
              </div>
            </div>
          )}

          <div className="mt-6 flex items-center gap-4">
            <label className="text-sm text-ink-600" htmlFor="qty">Qty</label>
            <input id="qty" type="number" min={1} max={Math.max(available, 1)} value={quantity}
                   onChange={(e) => setQuantity(Math.max(1, parseInt(e.target.value || '1', 10)))}
                   className="w-20 rounded border border-ink-100 px-2 py-1.5 text-sm" />
            <span className="text-xs text-ink-400">{available > 0 ? `${available} in stock` : 'Out of stock'}</span>
          </div>

          <ErrorBanner message={error} />

          <div className="mt-6 flex gap-3">
            <button onClick={handleAddToCart} disabled={busy || available <= 0}
                    className="flex-1 rounded-md bg-ink-900 px-6 py-3 text-sm font-semibold text-linen hover:bg-ink-800 disabled:opacity-50 transition-colors">
              Add to cart
            </button>
            <button onClick={handleBuyNow} disabled={busy || available <= 0}
                    className="flex-1 rounded-md bg-brass-500 px-6 py-3 text-sm font-semibold text-ink-900 hover:bg-brass-400 disabled:opacity-50 transition-colors">
              Buy now
            </button>
            <button onClick={handleWishlist} aria-label="Add to wishlist"
                    className="rounded-md border border-ink-100 px-4 py-3 hover:border-brass-400">
              ♡
            </button>
          </div>

          {product.specifications && Object.keys(product.specifications).length > 0 && (
            <div className="mt-10">
              <h3 className="font-medium text-ink-900 mb-3">Specifications</h3>
              <dl className="grid grid-cols-2 gap-y-2 text-sm">
                {Object.entries(product.specifications).map(([k, v]) => (
                  <React.Fragment key={k}>
                    <dt className="text-ink-400">{k}</dt>
                    <dd className="text-ink-900">{v}</dd>
                  </React.Fragment>
                ))}
              </dl>
            </div>
          )}

          <div className="mt-10">
            <h3 className="font-medium text-ink-900 mb-3">Description</h3>
            <p className="text-sm text-ink-600 whitespace-pre-line">{product.description}</p>
          </div>
        </div>
      </div>

      <section className="mt-16">
        <h2 className="font-display text-2xl text-ink-900 mb-6">Reviews</h2>
        {user && (
          <form onSubmit={submitReview} className="mb-8 max-w-lg space-y-3 rounded-lg border border-ink-100 p-5">
            <h3 className="font-medium text-ink-900">Write a review</h3>
            <ErrorBanner message={reviewError} />
            {reviewSuccess && <p className="text-sm text-green-700">{reviewSuccess}</p>}
            <div>
              <label className="text-sm text-ink-600">Rating</label>
              <select value={reviewForm.rating} onChange={(e) => setReviewForm({ ...reviewForm, rating: Number(e.target.value) })}
                      className="mt-1 block rounded border border-ink-100 px-2 py-1.5 text-sm">
                {[5, 4, 3, 2, 1].map((n) => <option key={n} value={n}>{n} star{n > 1 ? 's' : ''}</option>)}
              </select>
            </div>
            <input required placeholder="Title" value={reviewForm.title}
                   onChange={(e) => setReviewForm({ ...reviewForm, title: e.target.value })}
                   className="w-full rounded border border-ink-100 px-3 py-2 text-sm" />
            <textarea required placeholder="Share your experience" rows={3} value={reviewForm.reviewText}
                      onChange={(e) => setReviewForm({ ...reviewForm, reviewText: e.target.value })}
                      className="w-full rounded border border-ink-100 px-3 py-2 text-sm" />
            <button type="submit" className="rounded-md bg-ink-900 px-5 py-2 text-sm font-medium text-linen hover:bg-ink-800">
              Submit review
            </button>
          </form>
        )}

        {reviews?.content?.length ? (
          <ul className="space-y-6 max-w-2xl">
            {reviews.content.map((r) => (
              <li key={r.id} className="border-b border-ink-100 pb-6">
                <div className="flex items-center gap-2">
                  <span className="text-brass-500">{'★'.repeat(r.rating)}{'☆'.repeat(5 - r.rating)}</span>
                  <span className="font-medium text-ink-900">{r.title}</span>
                  {r.verifiedPurchase && <span className="text-xs text-green-700 bg-green-50 px-2 py-0.5 rounded">Verified purchase</span>}
                </div>
                <p className="mt-1 text-sm text-ink-600">{r.reviewText}</p>
                <p className="mt-1 text-xs text-ink-400">{r.customerDisplayName} · {formatDate(r.createdAt)}</p>
              </li>
            ))}
          </ul>
        ) : (
          <p className="text-sm text-ink-400">No reviews yet. Be the first to share your thoughts.</p>
        )}
      </section>

      {related.length > 0 && (
        <section className="mt-16">
          <h2 className="font-display text-2xl text-ink-900 mb-6">You may also like</h2>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
            {related.map((p) => <ProductCard key={p.id} product={p} />)}
          </div>
        </section>
      )}
    </div>
  )
}
