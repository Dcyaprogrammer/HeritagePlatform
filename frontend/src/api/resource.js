import api from './auth.js'

export const getMySubmissions = () => api.get('/resources/submissions')

export const getFeedback = (id) => api.get(`/resources/${id}/feedback`)

export const getReviewHistory = (id) => api.get(`/resources/${id}/history`)

export const resubmitResource = (id) => api.post(`/resources/${id}/resubmit`)

export const createDraft = (data) => api.post('/resources', data)

export const updateDraft = (id, data) => api.put(`/resources/${id}`, data)

export const submitForReview = (id) => api.post(`/resources/${id}/submit`)

export const getCategories = () => api.get('/public/categories')

export const getTags = () => api.get('/public/tags')

export const getHeritageTypeGroups = () => api.get('/public/heritage-type-groups')

export const getComments = (resourceId) => api.get(`/public/resources/${resourceId}/comments`)

export const addComment = (resourceId, content) => api.post(`/resources/${resourceId}/comments`, { content })