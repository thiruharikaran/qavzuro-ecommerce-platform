import client from './client'

export const returnApi = {
  create: (payload) => client.post('/returns', payload).then(r => r.data),
  mine: (page = 0, size = 10) => client.get('/returns/me', { params: { page, size } }).then(r => r.data),
  listAll: (page = 0, size = 20) => client.get('/returns', { params: { page, size } }).then(r => r.data),
  review: (id, payload) => client.patch(`/returns/${id}/review`, payload).then(r => r.data),
}
