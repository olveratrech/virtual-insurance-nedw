export function simulatePayment(cardNumber) {
  const normalizedCardNumber = cardNumber.replace(/\s/g, '')

  if (normalizedCardNumber === '4000000000000000') {
    return { status: 'REJECTED', message: 'El pago fue rechazado por el simulador. No se emitió ninguna póliza.' }
  }

  return { status: 'APPROVED', authorizationCode: 'AUTH-260001' }
}
