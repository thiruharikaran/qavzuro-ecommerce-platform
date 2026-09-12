import client from './client'

export const orderApi = {
  checkout: (payload) => client.post('/checkout', payload).then(r => r.data),
  myOrders: (page = 0, size = 10) => client.get('/orders/me', { params: { page, size } }).then(r => r.data),
  getById: (id) => client.get(`/orders/${id}`).then(r => r.data),
  getByOrderNumber: (num) => client.get(`/orders/number/${num}`).then(r => r.data),
  cancel: (id) => client.post(`/orders/${id}/cancel`).then(r => r.data),
  listAll: (page = 0, size = 20) => client.get('/orders', { params: { page, size } }).then(r => r.data),
  updateStatus: (id, payload) => client.patch(`/orders/${id}/status`, payload).then(r => r.data),
}
