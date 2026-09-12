import client from './client'

export const adminApi = {
  dashboard: () => client.get('/admin/dashboard').then(r => r.data),
  auditLogs: (page = 0, size = 50) => client.get('/audit', { params: { page, size } }).then(r => r.data),
  roles: () => client.get('/roles').then(r => r.data),
  createRole: (payload) => client.post('/roles', payload).then(r => r.data),
  updateRolePermissions: (id, permissionCodes) => client.put(`/roles/${id}/permissions`, permissionCodes).then(r => r.data),
  permissions: () => client.get('/permissions').then(r => r.data),
}
