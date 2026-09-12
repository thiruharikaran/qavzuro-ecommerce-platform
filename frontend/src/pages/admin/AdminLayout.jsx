import React from 'react'
import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'

const NAV_ITEMS = [
  { to: '/admin', label: 'Dashboard', end: true, perm: 'REPORT_VIEW' },
  { to: '/admin/products', label: 'Products', perm: 'PRODUCT_VIEW' },
  { to: '/admin/categories', label: 'Categories', perm: 'CATEGORY_MANAGE' },
  { to: '/admin/orders', label: 'Orders', perm: 'ORDER_VIEW' },
  { to: '/admin/returns', label: 'Returns', perm: 'RETURN_VIEW' },
  { to: '/admin/reviews', label: 'Reviews', perm: 'REVIEW_MODERATE' },
  { to: '/admin/coupons', label: 'Coupons', perm: 'COUPON_MANAGE' },
  { to: '/admin/inventory', label: 'Inventory', perm: 'INVENTORY_VIEW' },
  { to: '/admin/users', label: 'Users', perm: 'USER_VIEW' },
  { to: '/admin/roles', label: 'Roles & Permissions', perm: 'ROLE_MANAGE' },
  { to: '/admin/workforce', label: 'Workforce', perm: 'WORKFORCE_VIEW' },
  { to: '/admin/audit', label: 'Audit Log', perm: 'AUDIT_VIEW' },
]

export default function AdminLayout() {
  const { hasPermission, user } = useAuth()
  const visibleItems = NAV_ITEMS.filter((item) => hasPermission(item.perm) || hasPermission('MASTER_ADMIN_ALL'))

  return (
    <div className="mx-auto max-w-7xl px-4 py-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="font-display text-2xl text-ink-900">Admin</h1>
        <span className="text-xs text-ink-400">{user?.roleCodes?.join(', ')}</span>
      </div>
      <div className="grid md:grid-cols-[210px_1fr] gap-8">
        <nav aria-label="Admin navigation" className="flex md:flex-col gap-1 overflow-x-auto md:overflow-visible">
          {visibleItems.map((item) => (
            <NavLink key={item.to} to={item.to} end={item.end}
                     className={({ isActive }) => `whitespace-nowrap rounded-md px-3 py-2 text-sm ${isActive ? 'bg-ink-900 text-linen' : 'text-ink-600 hover:bg-ink-50'}`}>
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className="min-w-0">
          <Outlet />
        </div>
      </div>
    </div>
  )
}
