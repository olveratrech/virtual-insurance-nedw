const controlKeys = new Set(['Backspace', 'Delete', 'Tab', 'Enter', 'Escape', 'ArrowLeft', 'ArrowRight', 'ArrowUp', 'ArrowDown', 'Home', 'End'])

export function sanitizeAlphabetic(value) {
  return value
    .replace(/[^a-zA-ZáéíóúÁÉÍÓÚüÜñÑ\s'-]/g, '')
    .replace(/\s{2,}/g, ' ')
}

export function sanitizeNumeric(value, maxLength) {
  return value.replace(/\D/g, '').slice(0, maxLength)
}

export function sanitizeVehicleText(value, maxLength) {
  return value
    .toUpperCase()
    .replace(/[^A-ZÁÉÍÓÚÜÑ0-9\s'-]/g, '')
    .replace(/\s{2,}/g, ' ')
    .slice(0, maxLength)
}

export function sanitizeIdentifier(value, maxLength) {
  return value.toUpperCase().replace(/[^A-Z0-9&-]/g, '').slice(0, maxLength)
}

export function formatCardNumber(value, maxDigits = 16) {
  return sanitizeNumeric(value, maxDigits).replace(/(.{4})/g, '$1 ').trim()
}

export function formatExpiration(value) {
  const digits = sanitizeNumeric(value, 4)
  if (!digits) return ''
  if (digits.length === 1) {
    return /^[2-9]$/.test(digits) ? `0${digits}/` : digits
  }
  return `${digits.slice(0, 2)}/${digits.slice(2)}`
}

export function preventInvalidKey(event, allowedCharacter) {
  if (event.ctrlKey || event.metaKey || event.altKey || controlKeys.has(event.key)) return
  if (event.key.length === 1 && !allowedCharacter.test(event.key)) event.preventDefault()
}

export function replaceFromPaste(event, sanitizer, updateValue) {
  event.preventDefault()
  updateValue(sanitizer(event.clipboardData.getData('text')))
}

export function focusFirstInvalid(form) {
  requestAnimationFrame(() => form?.querySelector('[aria-invalid="true"]')?.focus())
}
