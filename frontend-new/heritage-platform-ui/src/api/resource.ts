import type { Comment, ResourceDetail } from '@/types/resource'
import {
  getComments as getMockComments,
  getResourceById as getMockResourceById,
  postComment as postMockComment,
} from '@/api/mockData'
import { devAuth } from '@/config/auth'

interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'
const USE_MOCK_FALLBACK =
  (import.meta.env.VITE_USE_MOCK_FALLBACK ?? String(import.meta.env.DEV)).toLowerCase() === 'true'

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(init?.headers ?? {}),
    },
    ...init,
  })
  const body = (await res.json()) as ApiResponse<T>
  if (!res.ok || body.code !== 200) {
    throw new Error(body.message || `Request failed: ${res.status}`)
  }
  return body.data
}

export function getResourceById(id: number) {
  return request<ResourceDetail>(`/api/resources/${id}`).catch((err) => {
    if (!USE_MOCK_FALLBACK) throw err
    const resource = getMockResourceById(id)
    if (!resource) throw err
    return resource
  })
}

export function getComments(resourceId: number) {
  return request<Comment[]>(`/api/resources/${resourceId}/comments`).catch((err) => {
    if (!USE_MOCK_FALLBACK) throw err
    return getMockComments(resourceId)
  })
}

export function postComment(resourceId: number, userId: number, content: string) {
  return request<Comment>(`/api/resources/${resourceId}/comments`, {
    method: 'POST',
    headers: {
      'X-User-Id': String(userId),
    },
    body: JSON.stringify({ content }),
  }).catch((err) => {
    if (!USE_MOCK_FALLBACK) throw err
    return postMockComment(resourceId, userId, content, devAuth.userName)
  })
}

