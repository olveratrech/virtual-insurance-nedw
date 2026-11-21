import { ApiError, apiRequest } from './apiClient.js'

let csrf = null

async function getCsrf() {
  if (csrf) return csrf
  csrf = await apiRequest('/auth/csrf')
  return csrf
}

async function authenticatedRequest(path, options = {}) {
  const csrfData = await getCsrf()

  return apiRequest(path, {
    ...options,
    headers: {
      ...options.headers,
      [csrfData.headerName]: csrfData.token,
    },
  })
}

export async function getCurrentSession() {
  try {
    return await apiRequest('/auth/me')
  } catch (error) {
    if (error instanceof ApiError && error.status === 401) return null
    throw error
  }
}

export function login(credentials) {
  return authenticatedRequest('/auth/login', {
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

  return authenticatedRequest('/auth/registro', {
    method: 'POST',
    body: formData,
  })
}

export async function logout() {
  try {
    await authenticatedRequest('/auth/logout', { method: 'POST' })
  } finally {
    csrf = null
  }
}
