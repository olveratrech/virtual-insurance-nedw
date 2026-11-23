import { useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import CheckboxField from '../../ui/CheckboxField/CheckboxField.jsx'
import FormField from '../../ui/FormField/FormField.jsx'
import SelectField from '../../ui/SelectField/SelectField.jsx'
import { getTodayInputValue, isCardExpirationExpired, isPastDate } from '../../../utils/dateValidation.js'
import { focusFirstInvalid, formatCardNumber, formatExpiration, preventInvalidKey, replaceFromPaste, sanitizeAlphabetic, sanitizeNumeric } from '../../../utils/inputHygiene.js'
import './PaymentForm.css'

const initialValues = { coverageStart: '', paymentMode: 'MONTHLY', cardholderName: '', cardBrand: '', cardNumber: '', expiration: '', cvv: '', acceptsTerms: false }
const cardRules = {
  VISA: { number: /^4\d{15}$/, numberLength: 16, cvvLength: 3 },
  MASTERCARD: { number: /^(?:5[1-5]\d{14}|2(?:2(?:2[1-9]|[3-9]\d)|[3-6]\d{2}|7(?:[01]\d|20))\d{12})$/, numberLength: 16, cvvLength: 3 },
  AMEX: { number: /^3[47]\d{13}$/, numberLength: 15, cvvLength: 4 },
}

function PaymentForm({ onPay, paymentError, backDestination, isSubmitting }) {
  const formRef = useRef(null)
  const [values, setValues] = useState(initialValues)
  const [errors, setErrors] = useState({})
  const today = getTodayInputValue()

  function updateField(event) {
    const { name, type, checked, value } = event.target
    setValues((current) => {
      if (type === 'checkbox') return { ...current, [name]: checked }
      if (name === 'cardholderName') return { ...current, cardholderName: sanitizeAlphabetic(value).slice(0, 100) }
      if (name === 'cardNumber') return { ...current, cardNumber: formatCardNumber(value, current.cardBrand === 'AMEX' ? 15 : 16) }
      if (name === 'expiration') return { ...current, expiration: formatExpiration(value) }
      if (name === 'cvv') return { ...current, cvv: sanitizeNumeric(value, 4) }
      if (name === 'cardBrand') {
        return {
          ...current,
          cardBrand: value,
          cardNumber: formatCardNumber(current.cardNumber, value === 'AMEX' ? 15 : 16),
          cvv: sanitizeNumeric(current.cvv, value === 'AMEX' ? 4 : 3),
        }
      }
      return { ...current, [name]: value }
    })
    setErrors((current) => ({ ...current, [name]: undefined }))
  }

  function setPastedValue(name, sanitizer) {
    return (event) => replaceFromPaste(event, sanitizer, (value) => {
      setValues((current) => ({ ...current, [name]: value }))
      setErrors((current) => ({ ...current, [name]: undefined }))
    })
  }

  function handleExpirationKeyDown(event) {
    if (event.key === 'Backspace' && values.expiration.endsWith('/')) {
      event.preventDefault()
      setValues((current) => ({ ...current, expiration: current.expiration.slice(0, -2) }))
      return
    }
    preventInvalidKey(event, /\d/)
  }

  function handleSubmit(event) {
    event.preventDefault()
    const errorsFound = {}
    const normalizedCard = values.cardNumber.replace(/\s/g, '')
    const cardRule = cardRules[values.cardBrand]
    if (!values.coverageStart) errorsFound.coverageStart = 'Selecciona el inicio de vigencia.'
    else if (isPastDate(values.coverageStart)) errorsFound.coverageStart = 'El inicio de vigencia no puede estar en el pasado.'
    if (!values.cardholderName.trim()) errorsFound.cardholderName = 'Ingresa el nombre de la tarjeta.'
    if (!cardRule) errorsFound.cardBrand = 'Selecciona la red de la tarjeta.'
    if (cardRule && !cardRule.number.test(normalizedCard)) {
      errorsFound.cardNumber = `El número no corresponde a ${values.cardBrand === 'AMEX' ? 'American Express' : values.cardBrand === 'MASTERCARD' ? 'Mastercard' : 'Visa'} (${cardRule.numberLength} dígitos).`
    }
    if (!/^(0[1-9]|1[0-2])\/\d{2}$/.test(values.expiration)) errorsFound.expiration = 'Utiliza el formato MM/AA.'
    else if (isCardExpirationExpired(values.expiration)) errorsFound.expiration = 'La tarjeta está vencida.'
    if (cardRule && !new RegExp(`^\\d{${cardRule.cvvLength}}$`).test(values.cvv)) {
      errorsFound.cvv = `Ingresa ${cardRule.cvvLength} dígitos.`
    }
    if (!values.acceptsTerms) errorsFound.acceptsTerms = 'Debes aceptar los términos para continuar.'
    setErrors(errorsFound)
    if (Object.keys(errorsFound).length > 0) {
      focusFirstInvalid(formRef.current)
      return
    }
    if (!isSubmitting) onPay(values)
  }

  const hasErrors = Object.values(errors).some(Boolean)

  return (
    <form ref={formRef} className="payment-form" onSubmit={handleSubmit} noValidate>
      <fieldset className="payment-form__section">
        <legend>Datos de contratación</legend>
        <div className="payment-form__grid">
          <FormField id="coverage-start" label="Inicio de vigencia" name="coverageStart" type="date" min={today} data-technical="true" value={values.coverageStart} onChange={updateField} error={errors.coverageStart} />
          <SelectField id="payment-mode" label="Modalidad de pago" name="paymentMode" value={values.paymentMode} onChange={updateField}>
            <option value="MONTHLY">Mensual</option><option value="ANNUAL">Anual</option>
          </SelectField>
        </div>
      </fieldset>
      <fieldset className="payment-form__section">
        <legend>Datos del medio de pago</legend>
        <FormField id="cardholder-name" label="Nombre en la tarjeta" name="cardholderName" autoComplete="cc-name" maxLength="100" value={values.cardholderName} onChange={updateField} onKeyDown={(event) => preventInvalidKey(event, /[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ\s'-]/)} onPaste={setPastedValue('cardholderName', (value) => sanitizeAlphabetic(value).slice(0, 100))} error={errors.cardholderName} />
        <div className="payment-form__grid">
          <SelectField id="card-brand" label="Red de la tarjeta" name="cardBrand" value={values.cardBrand} onChange={updateField} error={errors.cardBrand}>
            <option value="">Selecciona una red</option>
            <option value="VISA">Visa</option>
            <option value="MASTERCARD">Mastercard</option>
            <option value="AMEX">American Express</option>
          </SelectField>
          <FormField id="card-number" label="Número de tarjeta" name="cardNumber" inputMode="numeric" autoComplete="cc-number" placeholder={values.cardBrand === 'AMEX' ? '3714 4963 5398 431' : '4242 4242 4242 4242'} maxLength="19" data-technical="true" value={values.cardNumber} onChange={updateField} onKeyDown={(event) => preventInvalidKey(event, /\d/)} onPaste={setPastedValue('cardNumber', (value) => formatCardNumber(value, values.cardBrand === 'AMEX' ? 15 : 16))} error={errors.cardNumber} />
        </div>
        <div className="payment-form__grid">
          <FormField id="card-expiration" label="Vencimiento" name="expiration" inputMode="numeric" autoComplete="cc-exp" placeholder="MM/AA" maxLength="5" data-technical="true" value={values.expiration} onChange={updateField} onKeyDown={handleExpirationKeyDown} onPaste={setPastedValue('expiration', formatExpiration)} error={errors.expiration} />
          <FormField id="card-cvv" label="CVV" name="cvv" type="password" inputMode="numeric" autoComplete="cc-csc" maxLength="4" data-technical="true" value={values.cvv} onChange={updateField} onKeyDown={(event) => preventInvalidKey(event, /\d/)} onPaste={setPastedValue('cvv', (value) => sanitizeNumeric(value, 4))} error={errors.cvv} />
        </div>
      </fieldset>
      <div>
        <CheckboxField id="payment-terms" name="acceptsTerms" checked={values.acceptsTerms} onChange={updateField} label="Acepto términos, condiciones y aviso de privacidad" />
        {errors.acceptsTerms && <p className="form-field__error">{errors.acceptsTerms}</p>}
      </div>
      {paymentError && <p className="payment-form__error" role="alert">{paymentError}</p>}
      <div className="payment-form__actions">
        <Link className="button button--secondary" to={backDestination}>Volver</Link>
        <button className="button button--primary" type="submit" disabled={isSubmitting || hasErrors}>
          {isSubmitting ? 'Procesando pago…' : 'Pagar y emitir póliza'}
        </button>
      </div>
    </form>
  )
}

export default PaymentForm
