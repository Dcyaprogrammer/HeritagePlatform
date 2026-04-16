import axios from 'axios'

// Create axios instance with base configuration
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Request interceptor for JWT token injection
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor for unified error handling
api.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    console.error('API Error:', error.response?.data || error.message)
    return Promise.reject(error)
  }
)

// ============================================
// User Profile APIs
// ============================================

/**
 * Get user by username
 * @param {string} username - Username
 * @returns {Promise} User data
 */
export const getUser = (username) => api.get(`/user/${username}`)

/**
 * Get user by ID
 * @param {number} userId - User ID
 * @returns {Promise} User data
 */
export const getUserById = (userId) => api.get(`/users/${userId}`)

/**
 * Update user profile
 * @param {string} username - Username
 * @param {object} data - Update data { displayName, email, avatar, bio }
 * @returns {Promise} Updated user data
 */
export const updateUser = (username, data) => api.put(`/user/${username}`, data)

/**
 * Change user password
 * @param {string} username - Username
 * @param {object} data - Password data { oldPassword, newPassword }
 * @returns {Promise} Success message
 */
export const updatePassword = (username, data) => api.put(`/user/${username}/password`, data)

// ============================================
// User List & Search APIs
// ============================================

/**
 * Get all users (simple list)
 * @returns {Promise} Array of users
 */
export const getAllUsers = () => api.get('/users')

/**
 * Get paginated user list
 * @param {object} params - Query parameters { page, size, role, keyword }
 * @returns {Promise} Paginated user data
 */
export const getUserPage = (params) => api.get('/users/page', { params })

// ============================================
// Contributor Application APIs
// ============================================

/**
 * Apply to be a contributor
 * @param {string} username - Username
 * @param {string} reason - Application reason
 * @returns {Promise} Updated user data
 */
export const applyContributor = (username, reason) =>
  api.post(`/user/${username}/apply`, { reason })

/**
 * Get pending contributor applications
 * @returns {Promise} Array of pending applications
 */
export const getPendingApplications = () => api.get('/users/pending')

/**
 * Approve contributor application
 * @param {string} username - Username to approve
 * @returns {Promise} Updated user data
 */
export const approveContributor = (username) =>
  api.put(`/user/${username}/approve`)

/**
 * Reject contributor application
 * @param {string} username - Username to reject
 * @returns {Promise} Updated user data
 */
export const rejectContributor = (username) =>
  api.put(`/user/${username}/reject`)

// ============================================
// Admin APIs
// ============================================

/**
 * Update user role (Admin only)
 * @param {number} userId - User ID
 * @param {string} role - New role (ADMIN, CONTRIBUTOR, VIEWER)
 * @returns {Promise} Updated user data
 */
export const updateUserRole = (userId, role) =>
  api.put(`/admin/users/${userId}/role`, null, { params: { role } })

export default api
