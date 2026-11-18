import { useState } from 'react'
import { Link } from 'react-router-dom'
import CheckboxField from '../../ui/CheckboxField/CheckboxField.jsx'
import FormField from '../../ui/FormField/FormField.jsx'
import SelectField from '../../ui/SelectField/SelectField.jsx'
import './PaymentForm.css'

const initialValues = { coverageStart: '', paymentMode: 'MONTHLY', cardholderName: '', cardNumber: '', expiration: '', cvv: '', acceptsTerms: false }

function PaymentForm({ onPay, paymentError }) {
  const [values, setValues] = useState(initialValues)
  const [errors, setErrors] = useState({})

  function updateField(event) {
    const { name, type, checked, value } = event.target
    setValues((current) => ({ ...current, [name]: type === 'checkbox' ? checked : value }))
  }

  function handleSubmit(event) {
    event.preventDefault()
    const errorsFound = {}
    const normalizedCard = values.cardNumber.replace(/\s/g, '')
    if (!values.coverageStart) errorsFound.coverageStart = 'Selecciona el inicio de vigencia.'
    if (!values.cardholderName.trim()) errorsFound.cardholderName = 'Ingresa el nombre de la tarjeta.'
    if (!/^\d{16}$/.test(normalizedCard)) errorsFound.cardNumber = 'Ingresa 16 dígitos.'
    if (!/^(0[1-9]|1[0-2])\/\d{2}$/.test(values.expiration)) errorsFound.expiration = 'Utiliza el formato MM/AA.'
    if (!/^\d{3}$/.test(values.cvv)) errorsFound.cvv = 'Ingresa 3 dígitos.'
    if (!values.acceptsTerms) errorsFound.acceptsTerms = 'Debes aceptar los términos para continuar.'
    setErrors(errorsFound)
    if (Object.keys(errorsFound).length === 0) onPay(values)
  }

  return (
    <form className="payment-form" onSubmit={handleSubmit} noValidate>
      <div className="payment-form__grid">
        <FormField id="coverage-start" label="Inicio de vigencia" name="coverageStart" type="date" value={values.coverageStart} onChange={updateField} error={errors.coverageStart} />
        <SelectField id="payment-mode" label="Modalidad de pago" name="paymentMode" value={values.paymentMode} onChange={updateField}>
          <option value="MONTHLY">Mensual</option><option value="ANNUAL">Anual</option>
        </SelectField>
      </div>
      <FormField id="cardholder-name" label="Nombre en la tarjeta" name="cardholderName" autoComplete="cc-name" value={values.cardholderName} onChange={updateField} error={errors.cardholderName} />
      <FormField id="card-number" label="Número de tarjeta ficticia" name="cardNumber" inputMode="numeric" autoComplete="cc-number" placeholder="4242 4242 4242 4242" value={values.cardNumber} onChange={updateField} error={errors.cardNumber} />
      <div className="payment-form__grid">
        <FormField id="card-expiration" label="Vencimiento" name="expiration" autoComplete="cc-exp" placeholder="MM/AA" value={values.expiration} onChange={updateField} error={errors.expiration} />
        <FormField id="card-cvv" label="CVV ficticio" name="cvv" type="password" inputMode="numeric" autoComplete="cc-csc" value={values.cvv} onChange={updateField} error={errors.cvv} />
      </div>
      <div>
        <CheckboxField id="payment-terms" name="acceptsTerms" checked={values.acceptsTerms} onChange={updateField} label="Acepto términos, condiciones y aviso de privacidad" />
        {errors.acceptsTerms && <p className="form-field__error">{errors.acceptsTerms}</p>}
      </div>
      {paymentError && <p className="payment-form__error" role="alert">{paymentError}</p>}
      <div className="payment-form__actions">
        <Link className="button button--secondary" to="/cotizaciones/aprobada">Volver</Link>
        <button className="button button--primary" type="submit">Pagar y emitir póliza</button>
      </div>
    </form>
  )
}

export default PaymentForm
