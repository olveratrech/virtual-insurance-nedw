import { apiRequest } from './apiClient.js'

let csrf = null

async function getCsrf() {
  if (csrf) return csrf
  csrf = await apiRequest('/auth/csrf')
  return csrf
}

export async function csrfRequest(path, options = {}) {
  const csrfData = await getCsrf()

  return apiRequest(path, {
    ...options,
    headers: {
      ...options.headers,
      [csrfData.headerName]: csrfData.token,
    },
  })
}

export function clearCsrf() {
  csrf = null
}
