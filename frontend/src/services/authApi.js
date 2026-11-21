import { ApiError, apiRequest } from './apiClient.js'
import { clearCsrf, csrfRequest } from './csrfClient.js'

export async function getCurrentSession() {
  try {
    return await apiRequest('/auth/me')
  } catch (error) {
    if (error instanceof ApiError && error.status === 401) return null
    throw error
  }
}

export function login(credentials) {
  return csrfRequest('/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(credentials),
  })
}

export function register(data, photo) {
  const formData = new FormData()
  const json = new Blob([JSON.stringify(data)], { type: 'application/json' })
  formData.append('data', json, 'data.json')

  if (photo) formData.append('photo', photo)

  return csrfRequest('/auth/registro', {
    method: 'POST',
    body: formData,
  })
}

export async function logout() {
  try {
    await csrfRequest('/auth/logout', { method: 'POST' })
  } finally {
    clearCsrf()
  }
}
