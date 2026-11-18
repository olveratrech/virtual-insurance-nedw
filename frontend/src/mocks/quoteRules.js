export function evaluateAutoQuote(values) {
  const rejected = values.criminalRecord || Number(values.negativePoints) >= 12
  return rejected ? 'rejected' : 'approved'
}

export function evaluateMedicalQuote(values) {
  const riskFactors = [
    values.hypertension,
    values.glucoseOutOfRange,
    values.cholesterolOutOfRange,
    values.smoker,
    values.alcoholAddiction,
  ].filter(Boolean).length

  const rejected = Number(values.age) > 70 || riskFactors >= 3
  return rejected ? 'rejected' : 'approved'
}
