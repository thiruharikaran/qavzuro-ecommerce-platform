import React from 'react'
import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'

const NAV_ITEMS = [
  { to: '/account', label: 'Profile', end: true },
  { to: '/account/addresses', label: 'Addresses' },
  { to: '/account/orders', label: 'Orders' },
  { to: '/account/returns', label: 'Returns' },
  { to: '/account/notifications', label: 'Notifications' },
]

export default function AccountLayout() {
  const { user } = useAuth()

  return (
    <div className="mx-auto max-w-6xl px-4 py-10">
      <h1 className="font-display text-3xl text-ink-900 mb-1">My account</h1>
      <p className="text-sm text-ink-400 mb-8">{user?.email}</p>

      <div className="grid md:grid-cols-[200px_1fr] gap-10">
        <nav aria-label="Account navigation" className="flex md:flex-col gap-1 overflow-x-auto md:overflow-visible">
          {NAV_ITEMS.map((item) => (
            <NavLink key={item.to} to={item.to} end={item.end}
                     className={({ isActive }) => `whitespace-nowrap rounded-md px-3 py-2 text-sm ${isActive ? 'bg-ink-900 text-linen' : 'text-ink-600 hover:bg-ink-50'}`}>
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div>
          <Outlet />
        </div>
      </div>
    </div>
  )
}
