import client from './client'

export const authApi = {
  register: (payload) => client.post('/auth/register', payload).then(r => r.data),
  login: (payload) => client.post('/auth/login', payload).then(r => r.data),
  refresh: (refreshToken) => client.post('/auth/refresh', { refreshToken }).then(r => r.data),
  logout: (refreshToken) => client.post('/auth/logout', { refreshToken }).then(r => r.data),
  logoutAll: () => client.post('/auth/logout-all').then(r => r.data),
}
