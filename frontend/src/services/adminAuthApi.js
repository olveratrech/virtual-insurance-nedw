import { ApiError, apiRequest } from './apiClient.js'
import { clearCsrf, csrfRequest } from './csrfClient.js'

export async function getCurrentAdminSession() {
  try {
    return await apiRequest('/api/admin/auth/me')
  } catch (error) {
    if (error instanceof ApiError && [401, 403].includes(error.status)) return null
    throw error
  }
}

export function loginAdmin(credentials) {
  return csrfRequest('/api/admin/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(credentials),
  })
}

export async function logoutAdmin() {
  try {
    await csrfRequest('/api/admin/auth/logout', { method: 'POST' })
  } finally {
    clearCsrf()
  }
}
