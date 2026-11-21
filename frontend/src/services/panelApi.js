import { apiRequest } from './apiClient.js'

const statusPresentation = {
  APROBADA: { label: 'APROBADA', tone: 'success' },
  RECHAZADA: { label: 'RECHAZADA', tone: 'danger' },
  ACTIVA: { label: 'ACTIVA', tone: 'success' },
  CANCELACION_SOLICITADA: { label: 'CANCELACIÓN SOLICITADA', tone: 'warning' },
  CANCELADA: { label: 'CANCELADA', tone: 'danger' },
  VENCIDA: { label: 'VENCIDA', tone: 'neutral' },
  PENDIENTE: { label: 'PENDIENTE', tone: 'warning' },
  APROBADO: { label: 'APROBADO', tone: 'success' },
  RECHAZADO: { label: 'RECHAZADO', tone: 'danger' },
}

function mapActivity(item) {
  const presentation = statusPresentation[item.status] ?? {
    label: item.status,
    tone: 'neutral',
  }
  return {
    id: item.id,
    type: item.typeLabel,
    reference: item.reference,
    status: presentation.label,
    statusTone: presentation.tone,
    detail: item.detail,
    destination: item.destination,
  }
}

export async function getPanel() {
  const response = await apiRequest('/api/panel')
  return {
    clientName: response.clientName,
    recentActivity: response.recentActivity.map(mapActivity),
  }
}

export async function getAccountHistory() {
  const response = await apiRequest('/api/cuenta/historial')
  return {
    quotes: response.quotes.map(mapActivity),
    policies: response.policies.map(mapActivity),
    payments: response.payments.map(mapActivity),
  }
}
