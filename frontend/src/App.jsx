import React, { Suspense, lazy } from 'react'
import { Routes, Route } from 'react-router-dom'
import Navbar from './components/Navbar'
import Footer from './components/Footer'
import Spinner from './components/Spinner'
import ProtectedRoute from './routes/ProtectedRoute'

const Home = lazy(() => import('./pages/Home'))
const ProductList = lazy(() => import('./pages/ProductList'))
const ProductDetail = lazy(() => import('./pages/ProductDetail'))
const CartPage = lazy(() => import('./pages/CartPage'))
const CheckoutPage = lazy(() => import('./pages/CheckoutPage'))
const OrderConfirmation = lazy(() => import('./pages/OrderConfirmation'))
const WishlistPage = lazy(() => import('./pages/WishlistPage'))
const LoginPage = lazy(() => import('./pages/LoginPage'))
const RegisterPage = lazy(() => import('./pages/RegisterPage'))
const AccountLayout = lazy(() => import('./pages/account/AccountLayout'))
const AccountProfile = lazy(() => import('./pages/account/AccountProfile'))
const AccountAddresses = lazy(() => import('./pages/account/AccountAddresses'))
const AccountOrders = lazy(() => import('./pages/account/AccountOrders'))
const AccountOrderDetail = lazy(() => import('./pages/account/AccountOrderDetail'))
const AccountReturns = lazy(() => import('./pages/account/AccountReturns'))
const AccountNotifications = lazy(() => import('./pages/account/AccountNotifications'))
const WorkforceDashboard = lazy(() => import('./pages/workforce/WorkforceDashboard'))
const AdminLayout = lazy(() => import('./pages/admin/AdminLayout'))
const AdminDashboard = lazy(() => import('./pages/admin/AdminDashboard'))
const AdminProducts = lazy(() => import('./pages/admin/AdminProducts'))
const AdminProductForm = lazy(() => import('./pages/admin/AdminProductForm'))
const AdminCategories = lazy(() => import('./pages/admin/AdminCategories'))
const AdminOrders = lazy(() => import('./pages/admin/AdminOrders'))
const AdminOrderDetail = lazy(() => import('./pages/admin/AdminOrderDetail'))
const AdminReturns = lazy(() => import('./pages/admin/AdminReturns'))
const AdminReviews = lazy(() => import('./pages/admin/AdminReviews'))
const AdminCoupons = lazy(() => import('./pages/admin/AdminCoupons'))
const AdminInventory = lazy(() => import('./pages/admin/AdminInventory'))
const AdminUsers = lazy(() => import('./pages/admin/AdminUsers'))
const AdminRoles = lazy(() => import('./pages/admin/AdminRoles'))
const AdminWorkforce = lazy(() => import('./pages/admin/AdminWorkforce'))
const AdminAudit = lazy(() => import('./pages/admin/AdminAudit'))
const NotFound = lazy(() => import('./pages/NotFound'))

export default function App() {
  return (
    <div className="flex min-h-screen flex-col">
      <Navbar />
      <main className="flex-1">
        <Suspense fallback={<Spinner label="Loading page" />}>
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/products" element={<ProductList />} />
            <Route path="/products/:slug" element={<ProductDetail />} />
            <Route path="/cart" element={<CartPage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />

            <Route path="/checkout" element={<ProtectedRoute><CheckoutPage /></ProtectedRoute>} />
            <Route path="/orders/:orderNumber/confirmation" element={<ProtectedRoute><OrderConfirmation /></ProtectedRoute>} />
            <Route path="/wishlist" element={<ProtectedRoute><WishlistPage /></ProtectedRoute>} />

            <Route path="/account" element={<ProtectedRoute><AccountLayout /></ProtectedRoute>}>
              <Route index element={<AccountProfile />} />
              <Route path="addresses" element={<AccountAddresses />} />
              <Route path="orders" element={<AccountOrders />} />
              <Route path="orders/:id" element={<AccountOrderDetail />} />
              <Route path="returns" element={<AccountReturns />} />
              <Route path="notifications" element={<AccountNotifications />} />
            </Route>

            <Route path="/workforce" element={<ProtectedRoute requirePermission="WORKFORCE_TASK_UPDATE_OWN"><WorkforceDashboard /></ProtectedRoute>} />

            <Route path="/admin" element={<ProtectedRoute requirePermission="REPORT_VIEW"><AdminLayout /></ProtectedRoute>}>
              <Route index element={<AdminDashboard />} />
              <Route path="products" element={<AdminProducts />} />
              <Route path="products/new" element={<AdminProductForm />} />
              <Route path="products/:id/edit" element={<AdminProductForm />} />
              <Route path="categories" element={<AdminCategories />} />
              <Route path="orders" element={<AdminOrders />} />
              <Route path="orders/:id" element={<AdminOrderDetail />} />
              <Route path="returns" element={<AdminReturns />} />
              <Route path="reviews" element={<AdminReviews />} />
              <Route path="coupons" element={<AdminCoupons />} />
              <Route path="inventory" element={<AdminInventory />} />
              <Route path="users" element={<AdminUsers />} />
              <Route path="roles" element={<AdminRoles />} />
              <Route path="workforce" element={<AdminWorkforce />} />
              <Route path="audit" element={<AdminAudit />} />
            </Route>

            <Route path="*" element={<NotFound />} />
          </Routes>
        </Suspense>
      </main>
      <Footer />
    </div>
  )
}
