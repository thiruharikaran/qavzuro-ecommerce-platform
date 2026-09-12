import client from './client'

export const couponApi = {
  list: () => client.get('/coupons').then(r => r.data),
  create: (payload) => client.post('/coupons', payload).then(r => r.data),
  setActive: (id, active) => client.patch(`/coupons/${id}/active`, null, { params: { active } }).then(r => r.data),
}
