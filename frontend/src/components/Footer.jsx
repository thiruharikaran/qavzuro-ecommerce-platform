import React from 'react'
import { Link } from 'react-router-dom'

export default function Footer() {
  return (
    <footer className="mt-20 border-t border-ink-100 bg-ink-900 text-ink-100">
      <div className="mx-auto max-w-7xl px-4 py-12 grid grid-cols-2 md:grid-cols-4 gap-8 text-sm">
        <div className="col-span-2 md:col-span-1">
          <div className="font-display text-xl text-linen mb-3">Qavzuro</div>
          <p className="text-ink-400 max-w-xs">Considered goods across electronics, fashion, and the home — chosen carefully, delivered reliably.</p>
        </div>
        <div>
          <div className="font-medium text-linen mb-3">Shop</div>
          <ul className="space-y-2 text-ink-400">
            <li><Link to="/products" className="hover:text-brass-400">All products</Link></li>
            <li><Link to="/products?sort=newest" className="hover:text-brass-400">New arrivals</Link></li>
          </ul>
        </div>
        <div>
          <div className="font-medium text-linen mb-3">Account</div>
          <ul className="space-y-2 text-ink-400">
            <li><Link to="/account/orders" className="hover:text-brass-400">Your orders</Link></li>
            <li><Link to="/account/returns" className="hover:text-brass-400">Returns</Link></li>
            <li><Link to="/wishlist" className="hover:text-brass-400">Wishlist</Link></li>
          </ul>
        </div>
        <div>
          <div className="font-medium text-linen mb-3">Support</div>
          <ul className="space-y-2 text-ink-400">
            <li>Sandbox checkout — no real payments</li>
            <li>help@qavzuro.dev</li>
          </ul>
        </div>
      </div>
      <div className="border-t border-ink-800 py-4 text-center text-xs text-ink-400">
        © {new Date().getFullYear()} Qavzuro. Built as a demonstration platform.
      </div>
    </footer>
  )
}
