import { apiRequest } from './apiClient.js'
import { csrfRequest } from './csrfClient.js'

function createQuote(path, data) {
  return csrfRequest(path, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  })
}

export function createAutoQuote(data) {
  return createQuote('/api/cotizaciones/auto', data)
}

export function createMedicalQuote(data) {
  return createQuote('/api/cotizaciones/medicas', data)
}

export function getQuote(quoteId) {
  return apiRequest(`/api/cotizaciones/${quoteId}`)
}
