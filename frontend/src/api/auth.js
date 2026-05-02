import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
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
  localStorage.removeItem('roles')
}

export const getToken = () => localStorage.getItem('token')

export const setToken = (token) => localStorage.setItem('token', token)

export const getStoredRoles = () => {
  const rawRoles = localStorage.getItem('roles')
  if (rawRoles) {
    try {
      const parsed = JSON.parse(rawRoles)
      if (Array.isArray(parsed)) {
        return parsed
      }
    } catch {
      // Fallback to the single-role storage below.
    }
  }

  const role = localStorage.getItem('role')
  return role ? [role] : []
}

export const setUserInfo = (username, roles) => {
  localStorage.setItem('username', username)
  const normalizedRoles = Array.isArray(roles) ? roles : [roles].filter(Boolean)
  localStorage.setItem('roles', JSON.stringify(normalizedRoles))
  localStorage.setItem('role', normalizedRoles[0] || '')
}



//reset，forgot
export const forgotPassword = (email) => {
  return api.post('/auth/forgot-password', { email })
}

export const resetPassword = (data) => {
  return api.post('/auth/reset-password', {
    token: data.token,
    newPassword: data.newPassword
  })
}





//多会话
export const getActiveSessions = () => {
  return api.get('/sessions')
}

export const logoutSession = (jti) => {
  return api.delete(`/sessions/${jti}`)
}

export default api
