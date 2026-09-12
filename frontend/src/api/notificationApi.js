import client from './client'

export const notificationApi = {
  list: (page = 0, size = 20) => client.get('/notifications', { params: { page, size } }).then(r => r.data),
  unreadCount: () => client.get('/notifications/unread-count').then(r => r.data),
  markRead: (id) => client.patch(`/notifications/${id}/read`).then(r => r.data),
  markAllRead: () => client.post('/notifications/read-all').then(r => r.data),
}
