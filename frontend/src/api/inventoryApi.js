import client from './client'

export const inventoryApi = {
  adjust: (productId, payload) => client.post(`/inventory/products/${productId}/adjust`, payload).then(r => r.data),
  history: (productId, page = 0, size = 20) =>
    client.get(`/inventory/products/${productId}/history`, { params: { page, size } }).then(r => r.data),
}
