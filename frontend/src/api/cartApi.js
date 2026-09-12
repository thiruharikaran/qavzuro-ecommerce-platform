import client from './client'

export const cartApi = {
  get: () => client.get('/cart').then(r => r.data),
  addItem: (payload) => client.post('/cart/items', payload).then(r => r.data),
  updateItem: (productId, variantId, quantity) =>
    client.put(`/cart/items/${productId}`, { quantity }, { params: variantId ? { variantId } : {} }).then(r => r.data),
  removeItem: (productId, variantId) =>
    client.delete(`/cart/items/${productId}`, { params: variantId ? { variantId } : {} }).then(r => r.data),
  clear: () => client.delete('/cart').then(r => r.data),
  applyCoupon: (code) => client.post('/cart/coupon', { code }).then(r => r.data),
  removeCoupon: () => client.delete('/cart/coupon').then(r => r.data),
}
