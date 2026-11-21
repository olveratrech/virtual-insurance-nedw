import { csrfRequest } from './csrfClient.js'

export function createCancellationRequest(policyId, data) {
  return csrfRequest(`/api/polizas/${policyId}/solicitudes-cancelacion`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  })
}
