import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

const client = axios.create({ baseURL: API_BASE_URL })

let accessToken = null
let refreshToken = null
let onAuthChange = null

export function setAuthTokens(tokens) {
  accessToken = tokens?.accessToken || null
  refreshToken = tokens?.refreshToken || null
  if (accessToken) localStorage.setItem('qavzuro_access_token', accessToken)
  else localStorage.removeItem('qavzuro_access_token')
  if (refreshToken) localStorage.setItem('qavzuro_refresh_token', refreshToken)
  else localStorage.removeItem('qavzuro_refresh_token')
}

export function loadStoredTokens() {
  accessToken = localStorage.getItem('qavzuro_access_token')
  refreshToken = localStorage.getItem('qavzuro_refresh_token')
  return { accessToken, refreshToken }
}

export function registerAuthChangeHandler(handler) {
  onAuthChange = handler
}

client.interceptors.request.use((config) => {
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`
  }
  return config
})

let refreshingPromise = null

client.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config
    if (error.response?.status === 401 && original && !original._retry && refreshToken) {
      original._retry = true
      try {
        if (!refreshingPromise) {
          refreshingPromise = axios
            .post(`${API_BASE_URL}/auth/refresh`, { refreshToken })
            .then((res) => res.data)
            .finally(() => { refreshingPromise = null })
        }
        const data = await refreshingPromise
        setAuthTokens(data)
        if (onAuthChange) onAuthChange(data)
        original.headers.Authorization = `Bearer ${data.accessToken}`
        return client(original)
      } catch (refreshError) {
        setAuthTokens(null)
        if (onAuthChange) onAuthChange(null)
        return Promise.reject(refreshError)
      }
    }
    return Promise.reject(error)
  }
)

export default client
