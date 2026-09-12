import client from './client'

export const userApi = {
  me: () => client.get('/users/me').then(r => r.data),
  updateProfile: (payload) => client.patch('/users/me', payload).then(r => r.data),
  changePassword: (payload) => client.post('/users/me/change-password', payload).then(r => r.data),
  addresses: () => client.get('/users/me/addresses').then(r => r.data),
  upsertAddress: (payload) => client.put('/users/me/addresses', payload).then(r => r.data),
  deleteAddress: (id) => client.delete(`/users/me/addresses/${id}`).then(r => r.data),
  list: (page = 0, size = 20) => client.get('/users', { params: { page, size } }).then(r => r.data),
  assignRoles: (id, roleCodes) => client.put(`/users/${id}/roles`, { roleCodes }).then(r => r.data),
  setEnabled: (id, enabled) => client.patch(`/users/${id}/enabled`, null, { params: { enabled } }).then(r => r.data),
}
