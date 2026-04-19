import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json'
  }
})

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

api.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('API Error:', error.response?.data || error.message)
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      localStorage.removeItem('role')
      window.location.href = '/login'
    }
    if (error.response?.status === 403) {
      alert('Permission denied. You do not have access to this resource.')
    }
    return Promise.reject(error)
  }
)

export const register = (userData) => api.post('/auth/register', userData)

export const login = (credentials) => api.post('/auth/login', credentials)

export const getCurrentUser = () => api.get('/auth/me')

export const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('role')
}

export const getToken = () => localStorage.getItem('token')

export const setToken = (token) => localStorage.setItem('token', token)

export const setUserInfo = (username, role) => {
  localStorage.setItem('username', username)
  localStorage.setItem('role', role)
}

export default api