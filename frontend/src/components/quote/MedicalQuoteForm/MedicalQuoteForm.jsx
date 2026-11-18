import { useState } from 'react'
import { Link } from 'react-router-dom'
import CheckboxField from '../../ui/CheckboxField/CheckboxField.jsx'
import FormField from '../../ui/FormField/FormField.jsx'
import './MedicalQuoteForm.css'

const initialValues = { age: '', hypertension: false, glucoseOutOfRange: false, cholesterolOutOfRange: false, smoker: false, alcoholAddiction: false }

function MedicalQuoteForm({ onEvaluate }) {
  const [values, setValues] = useState(initialValues)
  const [errors, setErrors] = useState({})

  function updateField(event) {
    const { name, type, checked, value } = event.target
    setValues((current) => ({ ...current, [name]: type === 'checkbox' ? checked : value }))
  }

  function handleSubmit(event) {
    event.preventDefault()
    const errorsFound = {}
    if (!values.age || Number(values.age) < 18 || Number(values.age) > 100) errorsFound.age = 'La edad debe estar entre 18 y 100 años.'
    setErrors(errorsFound)
    if (Object.keys(errorsFound).length === 0) onEvaluate(values)
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
      <div className="quote-form__actions">
        <Link className="button button--secondary" to="/panel">Volver al panel</Link>
        <button className="button button--primary" type="submit">Evaluar cotización</button>
      </div>
    </form>
  )
}

export default MedicalQuoteForm
