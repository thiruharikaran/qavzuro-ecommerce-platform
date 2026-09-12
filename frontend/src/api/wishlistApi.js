import client from './client'

export const wishlistApi = {
  get: () => client.get('/wishlist').then(r => r.data),
  add: (productId) => client.post(`/wishlist/${productId}`).then(r => r.data),
  remove: (productId) => client.delete(`/wishlist/${productId}`).then(r => r.data),
  moveToCart: (productId) => client.post(`/wishlist/${productId}/move-to-cart`).then(r => r.data),
}
