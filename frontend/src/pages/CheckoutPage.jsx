import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useCart } from '../context/CartContext'
import { userApi } from '../api/userApi'
import { orderApi } from '../api/orderApi'
import { formatMoney } from '../utils/format'
import ErrorBanner from '../components/ErrorBanner'
import Spinner from '../components/Spinner'

const EMPTY_ADDRESS = { label: '', fullName: '', phone: '', line1: '', line2: '', city: '', state: '', postalCode: '', country: 'India' }

export default function CheckoutPage() {
  const { cart, refresh } = useCart()
  const navigate = useNavigate()
  const [addresses, setAddresses] = useState(null)
  const [shippingAddressId, setShippingAddressId] = useState('')
  const [billingAddressId, setBillingAddressId] = useState('')
  const [shippingMethod, setShippingMethod] = useState('STANDARD')
  const [paymentMethod, setPaymentMethod] = useState('TEST_CARD')
  const [showNewAddress, setShowNewAddress] = useState(false)
  const [newAddress, setNewAddress] = useState(EMPTY_ADDRESS)
  const [error, setError] = useState('')
  const [placing, setPlacing] = useState(false)

  useEffect(() => {
    userApi.addresses().then((list) => {
      setAddresses(list)
      const defaultShip = list.find((a) => a.defaultShipping) || list[0]
      const defaultBill = list.find((a) => a.defaultBilling) || list[0]
      if (defaultShip) setShippingAddressId(defaultShip.id)
      if (defaultBill) setBillingAddressId(defaultBill.id)
      if (list.length === 0) setShowNewAddress(true)
    }).catch(() => setAddresses([]))
  }, [])

  async function saveNewAddress() {
    const saved = await userApi.upsertAddress({ ...newAddress, defaultShipping: true, defaultBilling: true })
    setAddresses(saved)
    const added = saved[saved.length - 1]
    setShippingAddressId(added.id)
    setBillingAddressId(added.id)
    setShowNewAddress(false)
    setNewAddress(EMPTY_ADDRESS)
  }

  async function placeOrder() {
    setError('')
    if (!shippingAddressId || !billingAddressId) {
      setError('Please select a shipping and billing address.')
      return
    }
    setPlacing(true)
    try {
      const order = await orderApi.checkout({ shippingAddressId, billingAddressId, shippingMethod, paymentMethod })
      await refresh()
      navigate(`/orders/${order.orderNumber}/confirmation`)
    } catch (e) {
      setError(e.response?.data?.message || 'Checkout failed. Please try again.')
    } finally {
      setPlacing(false)
    }
  }

  if (!cart || addresses === null) return <Spinner label="Preparing checkout" />
  if (cart.items.length === 0) { navigate('/cart'); return null }

  return (
    <div className="mx-auto max-w-5xl px-4 py-10">
      <h1 className="font-display text-3xl text-ink-900 mb-8">Checkout</h1>
      <div className="grid md:grid-cols-[1fr_320px] gap-10">
        <div className="space-y-8">
          <section>
            <h2 className="font-medium text-ink-900 mb-3">Shipping address</h2>
            <div className="space-y-2">
              {addresses.map((a) => (
                <label key={a.id} className={`flex gap-3 rounded-md border p-3 text-sm cursor-pointer ${shippingAddressId === a.id ? 'border-brass-500 bg-brass-50' : 'border-ink-100'}`}>
                  <input type="radio" name="shipping" checked={shippingAddressId === a.id} onChange={() => setShippingAddressId(a.id)} className="mt-1" />
                  <span>{a.fullName}, {a.line1}, {a.city}, {a.state} {a.postalCode}, {a.country} · {a.phone}</span>
                </label>
              ))}
              <button onClick={() => setShowNewAddress(!showNewAddress)} className="text-sm text-brass-600 hover:text-brass-500">
                + Add a new address
              </button>
            </div>
            {showNewAddress && (
              <div className="mt-4 grid grid-cols-2 gap-3 rounded-md border border-ink-100 p-4">
                {['fullName', 'phone', 'line1', 'line2', 'city', 'state', 'postalCode', 'country'].map((field) => (
                  <input key={field} placeholder={field} value={newAddress[field]}
                         onChange={(e) => setNewAddress({ ...newAddress, [field]: e.target.value })}
                         className="rounded border border-ink-100 px-3 py-2 text-sm col-span-1" />
                ))}
                <button onClick={saveNewAddress} className="col-span-2 rounded bg-ink-900 text-linen py-2 text-sm">Save address</button>
              </div>
            )}
          </section>

          <section>
            <h2 className="font-medium text-ink-900 mb-3">Billing address</h2>
            <select value={billingAddressId} onChange={(e) => setBillingAddressId(e.target.value)}
                    className="w-full rounded border border-ink-100 px-3 py-2 text-sm">
              {addresses.map((a) => <option key={a.id} value={a.id}>{a.fullName}, {a.line1}, {a.city}</option>)}
            </select>
          </section>

          <section>
            <h2 className="font-medium text-ink-900 mb-3">Shipping method</h2>
            <div className="space-y-2">
              <label className="flex items-center gap-2 text-sm"><input type="radio" checked={shippingMethod === 'STANDARD'} onChange={() => setShippingMethod('STANDARD')} /> Standard (3–5 days) — ₹49, free over ₹999</label>
              <label className="flex items-center gap-2 text-sm"><input type="radio" checked={shippingMethod === 'EXPRESS'} onChange={() => setShippingMethod('EXPRESS')} /> Express (1–2 days) — ₹149</label>
            </div>
          </section>

          <section>
            <h2 className="font-medium text-ink-900 mb-3">Payment (sandbox)</h2>
            <select value={paymentMethod} onChange={(e) => setPaymentMethod(e.target.value)} className="w-full rounded border border-ink-100 px-3 py-2 text-sm">
              <option value="TEST_CARD">Test card — succeeds</option>
              <option value="TEST_CARD_DECLINE">Test card — declines (for testing failure handling)</option>
            </select>
            <p className="mt-2 text-xs text-ink-400">No real payment details are collected. This is a sandbox integration.</p>
          </section>
        </div>

        <div className="rounded-lg border border-ink-100 p-6 h-fit">
          <h2 className="font-medium text-ink-900 mb-4">Order summary</h2>
          <ul className="space-y-2 text-sm mb-4">
            {cart.items.map((i) => (
              <li key={i.productId + (i.variantId || '')} className="flex justify-between">
                <span className="text-ink-600">{i.name} × {i.quantity}</span>
                <span>{formatMoney(i.lineTotal)}</span>
              </li>
            ))}
          </ul>
          <div className="space-y-1 text-sm border-t border-ink-100 pt-3">
            <div className="flex justify-between"><span className="text-ink-400">Subtotal</span><span>{formatMoney(cart.subtotal)}</span></div>
            {cart.discountTotal > 0 && <div className="flex justify-between text-green-700"><span>Discount</span><span>-{formatMoney(cart.discountTotal)}</span></div>}
            <div className="flex justify-between font-semibold text-ink-900 pt-2 border-t border-ink-100">
              <span>Estimated total</span><span>{formatMoney(cart.estimatedTotal)}</span>
            </div>
            <p className="text-xs text-ink-400">Final tax and shipping are calculated by the server at checkout.</p>
          </div>
          <ErrorBanner message={error} />
          <button onClick={placeOrder} disabled={placing}
                  className="mt-6 w-full rounded-md bg-brass-500 px-6 py-3 text-sm font-semibold text-ink-900 hover:bg-brass-400 disabled:opacity-50 transition-colors">
            {placing ? 'Placing order…' : 'Place order'}
          </button>
        </div>
      </div>
    </div>
  )
}
