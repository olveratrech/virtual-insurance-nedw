import { apiRequest } from './apiClient.js'
import { csrfRequest } from './csrfClient.js'

export function getProfile() {
  return apiRequest('/api/cuenta/perfil')
}

export function updateProfile(data) {
  return csrfRequest('/api/cuenta/perfil', {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  })
}

export function updateProfilePhoto(photo) {
  const formData = new FormData()
  formData.append('photo', photo)
  return csrfRequest('/api/cuenta/perfil/fotografia', { method: 'PUT', body: formData })
}

export function deleteProfilePhoto() {
  return csrfRequest('/api/cuenta/perfil/fotografia', { method: 'DELETE' })
}
