import client from './client'

export const reviewApi = {
  forProduct: (productId, page = 0, size = 10) =>
    client.get(`/reviews/product/${productId}`, { params: { page, size } }).then(r => r.data),
  create: (productId, payload) => client.post(`/reviews/product/${productId}`, payload).then(r => r.data),
  pending: (page = 0, size = 20) => client.get('/reviews/admin/pending', { params: { page, size } }).then(r => r.data),
  moderate: (id, status) => client.patch(`/reviews/${id}/moderate`, { status }).then(r => r.data),
}
