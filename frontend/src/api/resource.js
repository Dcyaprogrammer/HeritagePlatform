import api from './auth.js'

export const getMySubmissions = () => api.get('/resources/submissions')

export const getFeedback = (id) => api.get(`/resources/${id}/feedback`)

export const getReviewHistory = (id) => api.get(`/resources/${id}/history`)

export const resubmitResource = (id) => api.post(`/resources/${id}/resubmit`)
