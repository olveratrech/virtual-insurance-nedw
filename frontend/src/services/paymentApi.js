import { csrfRequest } from './csrfClient.js'

export function createPaymentAttempt(quoteId, data, idempotencyKey) {
  return csrfRequest(`/api/cotizaciones/${quoteId}/intentos-pago`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Idempotency-Key': idempotencyKey,
    },
    body: JSON.stringify(data),
  })
}

