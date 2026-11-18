export function getTodayInputValue(now = new Date()) {
  const localDate = new Date(now.getTime() - now.getTimezoneOffset() * 60_000)
  return localDate.toISOString().slice(0, 10)
}

export function isPastDate(value, now = new Date()) {
  return Boolean(value) && value < getTodayInputValue(now)
}

export function isCardExpirationExpired(value, now = new Date()) {
  const match = /^(0[1-9]|1[0-2])\/(\d{2})$/.exec(value)
  if (!match) return false

  const expirationMonth = Number(match[1])
  const expirationYear = 2000 + Number(match[2])
  const currentMonth = now.getMonth() + 1
  const currentYear = now.getFullYear()

  return expirationYear < currentYear
    || (expirationYear === currentYear && expirationMonth < currentMonth)
}
