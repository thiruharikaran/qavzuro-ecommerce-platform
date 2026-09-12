import React, { createContext, useContext, useState, useCallback, useEffect } from 'react'
import { cartApi } from '../api/cartApi'
import { useAuth } from './AuthContext'

const CartContext = createContext(null)

export function CartProvider({ children }) {
  const { user } = useAuth()
  const [cart, setCart] = useState(null)
  const [loading, setLoading] = useState(false)

  const refresh = useCallback(async () => {
    if (!user) { setCart(null); return }
    setLoading(true)
    try {
      const data = await cartApi.get()
      setCart(data)
    } finally {
      setLoading(false)
    }
  }, [user])

  useEffect(() => { refresh() }, [refresh])

  const addItem = useCallback(async (productId, variantId, quantity = 1) => {
    const data = await cartApi.addItem({ productId, variantId, quantity })
    setCart(data)
    return data
  }, [])

  const updateItem = useCallback(async (productId, variantId, quantity) => {
    const data = await cartApi.updateItem(productId, variantId, quantity)
    setCart(data)
    return data
  }, [])

  const removeItem = useCallback(async (productId, variantId) => {
    const data = await cartApi.removeItem(productId, variantId)
    setCart(data)
    return data
  }, [])

  const applyCoupon = useCallback(async (code) => {
    const data = await cartApi.applyCoupon(code)
    setCart(data)
    return data
  }, [])

  const removeCoupon = useCallback(async () => {
    const data = await cartApi.removeCoupon()
    setCart(data)
    return data
  }, [])

  const clear = useCallback(async () => {
    const data = await cartApi.clear()
    setCart(data)
    return data
  }, [])

  const itemCount = (cart?.items || []).reduce((sum, i) => sum + i.quantity, 0)

  return (
    <CartContext.Provider value={{ cart, loading, itemCount, refresh, addItem, updateItem, removeItem, applyCoupon, removeCoupon, clear }}>
      {children}
    </CartContext.Provider>
  )
}

export function useCart() {
  const ctx = useContext(CartContext)
  if (!ctx) throw new Error('useCart must be used within CartProvider')
  return ctx
}
