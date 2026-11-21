import { apiRequest } from './apiClient.js'

export async function getGenders() {
  const response = await apiRequest('/catalogos/generos')
  return response.items
}
