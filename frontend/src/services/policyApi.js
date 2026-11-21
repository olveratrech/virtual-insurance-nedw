import { apiRequest } from './apiClient.js'
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

