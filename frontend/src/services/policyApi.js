import { ApiError, apiRequest } from './apiClient.js'
import { formatMxn } from '../utils/currency.js'

function formatDate(value) {
  if (!value) return 'No aplica'
  return new Date(`${value}T12:00:00`).toLocaleDateString('es-MX', { dateStyle: 'long' })
}

export async function getPolicy(policyId) {
  const response = await apiRequest(`/api/polizas/${policyId}`)
  return {
    id: response.id,
    policyNumber: response.policyNumber,
    status: response.status,
    coverageActive: response.coverageActive,
    type: response.type,
    productName: response.productName,
    coverageName: response.coverageName,
    validFrom: formatDate(response.validFrom),
    validUntil: formatDate(response.validUntil),
    insuredLabel: response.insured.label,
    insuredValue: response.insured.value,
    paymentMode: response.payment.mode === 'ANNUAL' ? 'Anual' : 'Mensual',
    nextPayment: response.payment.nextPaymentDate
      ? formatDate(response.payment.nextPaymentDate)
      : 'Cubierto durante la vigencia',
    lastPayment: `${formatMxn(response.payment.lastPaymentAmount)} · ${response.payment.lastPaymentStatus}`,
    authorizationCode: response.payment.authorizationCode,
    documents: response.documents,
  }
}

export async function downloadPolicyDocument(downloadUrl) {
  let response
  try {
    response = await fetch(`/api${downloadUrl}`, { credentials: 'include' })
  } catch {
    throw new ApiError('No fue posible conectar con el servidor.', 0)
  }

  if (!response.ok) {
    const contentType = response.headers.get('content-type') ?? ''
    const problem = contentType.includes('json') ? await response.json() : null
    throw new ApiError(problem?.detail ?? 'No fue posible descargar el documento.', response.status, problem)
  }

  const disposition = response.headers.get('content-disposition') ?? ''
  const encodedFilename = disposition.match(/filename\*=UTF-8''([^;]+)/i)?.[1]
  const plainFilename = disposition.match(/filename="?([^";]+)"?/i)?.[1]
  const filename = encodedFilename
    ? decodeURIComponent(encodedFilename)
    : plainFilename ?? 'documento.pdf'

  return { blob: await response.blob(), filename }
}

