import client from './client'

export const productApi = {
  search: (params) => client.get('/products/search', { params }).then(r => r.data),
  getBySlug: (slug) => client.get(`/products/slug/${slug}`).then(r => r.data),
  getById: (id) => client.get(`/products/${id}`).then(r => r.data),
  related: (id) => client.get(`/products/${id}/related`).then(r => r.data),
  lowStock: () => client.get('/products/admin/low-stock').then(r => r.data),
  create: (payload) => client.post('/products', payload).then(r => r.data),
  update: (id, payload) => client.put(`/products/${id}`, payload).then(r => r.data),
  remove: (id) => client.delete(`/products/${id}`).then(r => r.data),
}
