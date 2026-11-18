export function createCancellationRequest(values) {
  return {
    requestNumber: 'CAN-0001',
    status: 'CANCELLATION_REQUESTED',
    requestedDate: values.requestedDate,
    reason: values.reason,
  }
}
