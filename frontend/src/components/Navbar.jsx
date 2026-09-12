import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useCart } from '../context/CartContext'

export default function Navbar() {
  const { user, logout, hasPermission } = useAuth()
  const { itemCount } = useCart()
  const [query, setQuery] = useState('')
  const [menuOpen, setMenuOpen] = useState(false)
  const navigate = useNavigate()

  const isStaff = hasPermission('ORDER_VIEW') || hasPermission('PRODUCT_CREATE') || hasPermission('MASTER_ADMIN_ALL')
  const isWorkforce = hasPermission('WORKFORCE_TASK_UPDATE_OWN') && !isStaff

  function submitSearch(e) {
    e.preventDefault()
    navigate(`/products?q=${encodeURIComponent(query)}`)
    setMenuOpen(false)
  }

  return (
    <header className="sticky top-0 z-40 bg-ink-900 text-linen">
      <div className="mx-auto max-w-7xl px-4">
        <div className="flex h-16 items-center justify-between gap-4">
          <Link to="/" className="font-display text-2xl tracking-tight text-linen shrink-0">
            Qavzuro
          </Link>

          <form onSubmit={submitSearch} className="hidden md:flex flex-1 max-w-xl">
            <input
              type="search"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Search products, brands, categories…"
              aria-label="Search products"
              className="w-full rounded-l-md border-0 bg-ink-800 px-4 py-2 text-sm text-linen placeholder:text-ink-400 focus:outline-none focus:ring-2 focus:ring-brass-400"
            />
            <button type="submit" className="rounded-r-md bg-brass-500 px-4 text-sm font-medium text-ink-900 hover:bg-brass-400 transition-colors">
              Search
            </button>
          </form>

          <nav className="hidden md:flex items-center gap-5 text-sm shrink-0">
            <Link to="/products" className="hover:text-brass-400 transition-colors">Shop</Link>
            {user ? (
              <>
                <Link to="/wishlist" className="hover:text-brass-400 transition-colors">Wishlist</Link>
                <Link to="/cart" className="relative hover:text-brass-400 transition-colors">
                  Cart
                  {itemCount > 0 && (
                    <span className="absolute -right-3 -top-2 flex h-4 w-4 items-center justify-center rounded-full bg-brass-500 text-[10px] font-semibold text-ink-900">
                      {itemCount}
                    </span>
                  )}
                </Link>
                <Link to="/account" className="hover:text-brass-400 transition-colors">Account</Link>
                {isWorkforce && <Link to="/workforce" className="hover:text-brass-400 transition-colors">Workforce</Link>}
                {isStaff && <Link to="/admin" className="hover:text-brass-400 transition-colors">Admin</Link>}
                <button onClick={logout} className="rounded-md border border-ink-600 px-3 py-1.5 hover:border-brass-400 hover:text-brass-400 transition-colors">
                  Sign out
                </button>
              </>
            ) : (
              <>
                <Link to="/cart" className="hover:text-brass-400 transition-colors">Cart</Link>
                <Link to="/login" className="rounded-md bg-brass-500 px-3 py-1.5 font-medium text-ink-900 hover:bg-brass-400 transition-colors">
                  Sign in
                </Link>
              </>
            )}
          </nav>

          <button className="md:hidden text-linen" onClick={() => setMenuOpen(!menuOpen)} aria-label="Toggle menu">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M4 6h16M4 12h16M4 18h16" strokeLinecap="round" />
            </svg>
          </button>
        </div>

        {menuOpen && (
          <div className="md:hidden pb-4 space-y-3">
            <form onSubmit={submitSearch} className="flex">
              <input
                type="search" value={query} onChange={(e) => setQuery(e.target.value)}
                placeholder="Search…" aria-label="Search products"
                className="w-full rounded-l-md border-0 bg-ink-800 px-4 py-2 text-sm text-linen placeholder:text-ink-400"
              />
              <button type="submit" className="rounded-r-md bg-brass-500 px-4 text-sm font-medium text-ink-900">Go</button>
            </form>
            <nav className="flex flex-col gap-3 text-sm">
              <Link to="/products" onClick={() => setMenuOpen(false)}>Shop</Link>
              {user ? (
                <>
                  <Link to="/wishlist" onClick={() => setMenuOpen(false)}>Wishlist</Link>
                  <Link to="/cart" onClick={() => setMenuOpen(false)}>Cart ({itemCount})</Link>
                  <Link to="/account" onClick={() => setMenuOpen(false)}>Account</Link>
                  {isWorkforce && <Link to="/workforce" onClick={() => setMenuOpen(false)}>Workforce</Link>}
                  {isStaff && <Link to="/admin" onClick={() => setMenuOpen(false)}>Admin</Link>}
                  <button onClick={() => { logout(); setMenuOpen(false) }} className="text-left">Sign out</button>
                </>
              ) : (
                <>
                  <Link to="/cart" onClick={() => setMenuOpen(false)}>Cart</Link>
                  <Link to="/login" onClick={() => setMenuOpen(false)}>Sign in</Link>
                </>
              )}
            </nav>
          </div>
        )}
      </div>
    </header>
  )
}
