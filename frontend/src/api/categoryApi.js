import client from './client'

export const categoryApi = {
  listActive: () => client.get('/categories').then(r => r.data),
  getBySlug: (slug) => client.get(`/categories/${slug}`).then(r => r.data),
  listAll: () => client.get('/categories/admin/all').then(r => r.data),
  create: (payload) => client.post('/categories', payload).then(r => r.data),
  update: (id, payload) => client.put(`/categories/${id}`, payload).then(r => r.data),
  setActive: (id, active) => client.patch(`/categories/${id}/active`, null, { params: { active } }).then(r => r.data),
}
