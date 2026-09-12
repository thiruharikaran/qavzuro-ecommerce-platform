import client from './client'

export const workforceApi = {
  assign: (payload) => client.post('/workforce/tasks', payload).then(r => r.data),
  myTasks: (page = 0, size = 20) => client.get('/workforce/tasks/assigned-to-me', { params: { page, size } }).then(r => r.data),
  tasksIAssigned: (page = 0, size = 20) => client.get('/workforce/tasks/assigned-by-me', { params: { page, size } }).then(r => r.data),
  updateStatus: (id, status) => client.patch(`/workforce/tasks/${id}/status`, { status }).then(r => r.data),
}
