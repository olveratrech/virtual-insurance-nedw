const mxnFormatter = new Intl.NumberFormat('es-MX', {
  style: 'currency',
  currency: 'MXN',
  minimumFractionDigits: 2,
})

export function formatMxn(value) {
  if (typeof value === 'string') return value
  if (typeof value !== 'number') return 'No disponible'
  return `${mxnFormatter.format(value)} MXN`
}

export function formatQuoteNumber(value) {
  if (!value) return 'No disponible'
  return String(value).startsWith('#') ? String(value) : `#${value}`
}
