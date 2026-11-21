import { apiRequest } from './apiClient.js'
import { csrfRequest } from './csrfClient.js'

export function getAdminCancellations(status = 'SOLICITADA') {
  return apiRequest(`/api/admin/solicitudes-cancelacion?status=${encodeURIComponent(status)}`)
}

export function getAdminCancellation(cancellationId) {
  return apiRequest(`/api/admin/solicitudes-cancelacion/${cancellationId}`)
}

export function resolveAdminCancellation(cancellationId, resolution) {
  return csrfRequest(`/api/admin/solicitudes-cancelacion/${cancellationId}/resolucion`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(resolution),
  })
}
