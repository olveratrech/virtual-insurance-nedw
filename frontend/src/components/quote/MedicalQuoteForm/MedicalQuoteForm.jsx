import { useState } from 'react'
import { Link } from 'react-router-dom'
import CheckboxField from '../../ui/CheckboxField/CheckboxField.jsx'
import FormField from '../../ui/FormField/FormField.jsx'
import './MedicalQuoteForm.css'

const defaultValues = { age: '', hypertension: false, glucoseOutOfRange: false, cholesterolOutOfRange: false, smoker: false, alcoholAddiction: false }

function MedicalQuoteForm({ onEvaluate, initialValues }) {
  const [values, setValues] = useState(() => ({ ...defaultValues, ...initialValues }))
  const [errors, setErrors] = useState({})
  const [submitError, setSubmitError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  function updateField(event) {
    const { name, type, checked, value } = event.target
    setValues((current) => ({ ...current, [name]: type === 'checkbox' ? checked : value }))
    setErrors((current) => ({ ...current, [name]: undefined }))
  }

  async function handleSubmit(event) {
    event.preventDefault()
    const errorsFound = {}
    if (!values.age || Number(values.age) < 18 || Number(values.age) > 100) errorsFound.age = 'La edad debe estar entre 18 y 100 años.'
    setErrors(errorsFound)
    setSubmitError('')
    if (Object.keys(errorsFound).length > 0) return

    const request = {
      age: Number(values.age),
      hypertension: values.hypertension,
      glucoseOutOfRange: values.glucoseOutOfRange,
      cholesterolOutOfRange: values.cholesterolOutOfRange,
      smoker: values.smoker,
      alcoholAddiction: values.alcoholAddiction,
    }

    setIsSubmitting(true)
    try {
      await onEvaluate(request)
    } catch (error) {
      const fieldErrors = error.problem?.fieldErrors ?? []
      setErrors((current) => ({
        ...current,
        ...Object.fromEntries(fieldErrors.map(({ field, message }) => [field, message])),
      }))
      setSubmitError(error.message)
    } finally {
      setIsSubmitting(false)
    }
  }

  const factors = [
    ['hypertension', 'Hipertensión'], ['glucoseOutOfRange', 'Glucosa fuera de rango'], ['cholesterolOutOfRange', 'Colesterol fuera de rango'], ['smoker', 'Tabaquismo'], ['alcoholAddiction', 'Adicción al alcohol'],
  ]

  return (
    <form className="quote-form" onSubmit={handleSubmit} noValidate>
      <fieldset className="quote-form__section">
        <legend>Información declarada del asegurado</legend>
        <div className="quote-form__grid">
          <FormField id="medical-age" label="Edad del asegurado" name="age" type="number" min="18" max="100" value={values.age} onChange={updateField} error={errors.age} />
        </div>
        <div className="medical-risk-grid">
          {factors.map(([name, label]) => <CheckboxField key={name} id={`medical-${name}`} name={name} checked={values[name]} onChange={updateField} label={label} description="Selecciona únicamente si corresponde." />)}
        </div>
      </fieldset>
      {submitError && <p className="quote-form__error" role="alert">{submitError}</p>}
      <div className="quote-form__actions">
        <Link className="button button--secondary" to="/panel">Volver al panel</Link>
        <button className="button button--primary" type="submit" disabled={isSubmitting}>
          {isSubmitting ? 'Evaluando…' : 'Evaluar cotización'}
        </button>
      </div>
    </form>
  )
}

export default MedicalQuoteForm
