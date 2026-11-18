import { useState } from 'react'
import { Link } from 'react-router-dom'
import CheckboxField from '../../ui/CheckboxField/CheckboxField.jsx'
import FormField from '../../ui/FormField/FormField.jsx'
import SelectField from '../../ui/SelectField/SelectField.jsx'
import TextareaField from '../../ui/TextareaField/TextareaField.jsx'
import { getTodayInputValue, isPastDate } from '../../../utils/dateValidation.js'
import './CancellationForm.css'

const initialValues = { reason: '', requestedDate: '', comments: '', confirmed: false }

function CancellationForm({ policyId, onSubmitRequest }) {
  const [values, setValues] = useState(initialValues)
  const [errors, setErrors] = useState({})
  const today = getTodayInputValue()

  function updateField(event) {
    const { name, type, checked, value } = event.target
    setValues((current) => ({ ...current, [name]: type === 'checkbox' ? checked : value }))
  }

  function handleSubmit(event) {
    event.preventDefault()
    const errorsFound = {}
    if (!values.reason) errorsFound.reason = 'Selecciona un motivo.'
    if (!values.requestedDate) errorsFound.requestedDate = 'Selecciona la fecha solicitada.'
    else if (isPastDate(values.requestedDate)) errorsFound.requestedDate = 'La fecha solicitada no puede estar en el pasado.'
    if (values.comments.length > 500) errorsFound.comments = 'Utiliza como máximo 500 caracteres.'
    if (!values.confirmed) errorsFound.confirmed = 'Debes confirmar que comprendes el proceso.'
    setErrors(errorsFound)
    if (Object.keys(errorsFound).length === 0) onSubmitRequest(values)
  }

  return (
    <form className="cancellation-form" onSubmit={handleSubmit} noValidate>
      <div className="cancellation-form__grid">
        <SelectField id="cancellation-reason" label="Motivo" name="reason" value={values.reason} onChange={updateField} error={errors.reason}>
          <option value="">Selecciona una opción</option><option value="NOT_NEEDED">Ya no necesito la cobertura</option><option value="FINANCIAL">Motivos económicos</option><option value="SERVICE">No estoy satisfecho con el servicio</option><option value="OTHER">Otro motivo</option>
        </SelectField>
        <FormField id="cancellation-date" label="Fecha solicitada" name="requestedDate" type="date" min={today} value={values.requestedDate} onChange={updateField} error={errors.requestedDate} />
      </div>
      <TextareaField id="cancellation-comments" label="Comentarios opcionales" name="comments" maxLength="500" placeholder="Describe brevemente el motivo de la solicitud." value={values.comments} onChange={updateField} error={errors.comments} />
      <div>
        <CheckboxField id="cancellation-confirmation" name="confirmed" checked={values.confirmed} onChange={updateField} label="Comprendo que la póliza seguirá activa durante la revisión" description="Enviar esta solicitud no cancela inmediatamente la cobertura." />
        {errors.confirmed && <p className="form-field__error">{errors.confirmed}</p>}
      </div>
      <aside className="cancellation-warning" role="note">El estado cambiará a <strong>CANCELACIÓN SOLICITADA</strong>. La cobertura seguirá activa hasta que la solicitud sea resuelta.</aside>
      <div className="cancellation-form__actions">
        <Link className="button button--secondary" to={`/polizas/${policyId}`}>Volver a la póliza</Link>
        <button className="button button--danger" type="submit">Enviar solicitud</button>
      </div>
    </form>
  )
}

export default CancellationForm
