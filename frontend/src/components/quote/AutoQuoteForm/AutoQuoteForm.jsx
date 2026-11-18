import { useState } from 'react'
import { Link } from 'react-router-dom'
import CheckboxField from '../../ui/CheckboxField/CheckboxField.jsx'
import FormField from '../../ui/FormField/FormField.jsx'
import SelectField from '../../ui/SelectField/SelectField.jsx'

const defaultValues = { brand: '', model: '', year: '', category: '', plates: '', licenseNumber: '', driverAge: '', negativePoints: '0', criminalRecord: false }

function AutoQuoteForm({ onEvaluate, initialValues }) {
  const [values, setValues] = useState(() => ({ ...defaultValues, ...initialValues }))
  const [errors, setErrors] = useState({})

  function updateField(event) {
    const { name, type, checked, value } = event.target
    setValues((current) => ({ ...current, [name]: type === 'checkbox' ? checked : value }))
  }

  function handleSubmit(event) {
    event.preventDefault()
    const errorsFound = {}
    if (!values.brand.trim()) errorsFound.brand = 'Ingresa la marca.'
    if (!values.model.trim()) errorsFound.model = 'Ingresa el modelo.'
    if (!values.category) errorsFound.category = 'Selecciona una categoría.'
    if (!values.year || Number(values.year) < 1980 || Number(values.year) > 2027) errorsFound.year = 'Ingresa un año entre 1980 y 2027.'
    if (!values.plates.trim()) errorsFound.plates = 'Ingresa las placas.'
    if (!values.licenseNumber.trim()) errorsFound.licenseNumber = 'Ingresa el número de licencia.'
    if (!values.driverAge || Number(values.driverAge) < 18 || Number(values.driverAge) > 90) errorsFound.driverAge = 'La edad debe estar entre 18 y 90 años.'
    if (Number(values.negativePoints) < 0) errorsFound.negativePoints = 'El valor no puede ser negativo.'
    setErrors(errorsFound)
    if (Object.keys(errorsFound).length === 0) onEvaluate(values)
  }

  return (
    <form className="quote-form" onSubmit={handleSubmit} noValidate>
      <fieldset className="quote-form__section">
        <legend>Datos del vehículo y conductor</legend>
        <div className="quote-form__grid">
          <FormField id="auto-brand" label="Marca" name="brand" value={values.brand} onChange={updateField} error={errors.brand} />
          <FormField id="auto-model" label="Modelo" name="model" value={values.model} onChange={updateField} error={errors.model} />
          <FormField id="auto-year" label="Año" name="year" type="number" min="1980" max="2027" value={values.year} onChange={updateField} error={errors.year} />
          <SelectField id="auto-category" label="Categoría" name="category" value={values.category} onChange={updateField} error={errors.category}>
            <option value="">Selecciona una opción</option><option value="SEDAN">Sedán</option><option value="SUV">SUV</option><option value="PICKUP">Pickup</option><option value="OTHER">Otra</option>
          </SelectField>
          <FormField id="auto-plates" label="Placas" name="plates" value={values.plates} onChange={updateField} error={errors.plates} />
          <FormField id="auto-license" label="Número de licencia" name="licenseNumber" value={values.licenseNumber} onChange={updateField} error={errors.licenseNumber} />
          <FormField id="auto-driver-age" label="Edad del conductor" name="driverAge" type="number" min="18" max="90" value={values.driverAge} onChange={updateField} error={errors.driverAge} />
          <FormField id="auto-negative-points" label="Puntos negativos" name="negativePoints" type="number" min="0" value={values.negativePoints} onChange={updateField} error={errors.negativePoints} />
        </div>
        <CheckboxField id="auto-criminal-record" name="criminalRecord" checked={values.criminalRecord} onChange={updateField} label="Tengo antecedentes penales" description="Esta respuesta forma parte de la evaluación académica del prototipo." />
      </fieldset>
      <div className="quote-form__actions">
        <Link className="button button--secondary" to="/panel">Volver al panel</Link>
        <button className="button button--primary" type="submit">Evaluar cotización</button>
      </div>
    </form>
  )
}

export default AutoQuoteForm
