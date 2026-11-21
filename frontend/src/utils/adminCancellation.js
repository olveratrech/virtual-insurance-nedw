const statusPresentation = {
  SOLICITADA: { label: 'Solicitada', tone: 'warning' },
  APROBADA: { label: 'Aprobada', tone: 'success' },
  RECHAZADA: { label: 'Rechazada', tone: 'danger' },
}

const reasonLabels = {
  NOT_NEEDED: 'Ya no necesita la cobertura',
  FINANCIAL: 'Motivos económicos',
  SERVICE: 'Inconformidad con el servicio',
  OTHER: 'Otro motivo',
}

export function presentCancellationStatus(status) {
  return statusPresentation[status] ?? { label: status, tone: 'neutral' }
}

export function presentCancellationReason(reason) {
  return reasonLabels[reason] ?? reason
}

export function formatAdminDate(value) {
  if (!value) return 'No disponible'
  const date = value.length === 10 ? new Date(`${value}T12:00:00`) : new Date(value)
  return date.toLocaleDateString('es-MX', { dateStyle: 'long' })
}

export function formatAdminDateTime(value) {
  if (!value) return 'No disponible'
  return new Date(value).toLocaleString('es-MX', { dateStyle: 'medium', timeStyle: 'short' })
}
