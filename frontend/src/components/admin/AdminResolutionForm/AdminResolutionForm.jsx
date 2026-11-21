import { useState } from 'react'
import TextareaField from '../../ui/TextareaField/TextareaField.jsx'
import './AdminResolutionForm.css'

function AdminResolutionForm({ onResolve }) {
  const [decision, setDecision] = useState('')
  const [observation, setObservation] = useState('')
  const [errors, setErrors] = useState({})
  const [submitError, setSubmitError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    const nextErrors = {}
    if (!decision) nextErrors.decision = 'Selecciona una decisión.'
    if (!observation.trim()) nextErrors.observation = 'Escribe la observación de la resolución.'
    if (observation.length > 500) nextErrors.observation = 'La observación admite hasta 500 caracteres.'
    setErrors(nextErrors)
    setSubmitError('')
    if (Object.keys(nextErrors).length) return

    const action = decision === 'APROBADA' ? 'aprobar' : 'rechazar'
    if (!window.confirm(`¿Confirmas que deseas ${action} esta solicitud? Esta decisión no puede modificarse.`)) return

    setIsSubmitting(true)
    try {
      await onResolve({ decision, observation: observation.trim() })
    } catch (error) {
      setSubmitError(error.message)
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <form className="admin-resolution-form" onSubmit={handleSubmit} noValidate>
      <div>
        <h2>Registrar resolución</h2>
        <p>La decisión actualizará inmediatamente el estado de la póliza.</p>
      </div>
      <fieldset aria-describedby={errors.decision ? 'admin-decision-error' : undefined}>
        <legend>Decisión</legend>
        <label className="admin-resolution-form__option">
          <input type="radio" name="decision" value="APROBADA" checked={decision === 'APROBADA'} onChange={(event) => setDecision(event.target.value)} />
          <span><strong>Aprobar cancelación</strong><small>La póliza cambiará a cancelada.</small></span>
        </label>
        <label className="admin-resolution-form__option">
          <input type="radio" name="decision" value="RECHAZADA" checked={decision === 'RECHAZADA'} onChange={(event) => setDecision(event.target.value)} />
          <span><strong>Rechazar cancelación</strong><small>La póliza regresará a activa.</small></span>
        </label>
        {errors.decision && <p className="form-field__error" id="admin-decision-error">{errors.decision}</p>}
      </fieldset>
      <TextareaField
        id="admin-resolution-observation"
        label="Observación"
        name="observation"
        rows="5"
        maxLength="500"
        value={observation}
        onChange={(event) => setObservation(event.target.value)}
        error={errors.observation}
      />
      <p className="admin-resolution-form__counter">{observation.length}/500 caracteres</p>
      {submitError && <p className="admin-resolution-form__error" role="alert">{submitError}</p>}
      <button className={decision === 'RECHAZADA' ? 'button button--danger' : 'button button--primary'} type="submit" disabled={isSubmitting}>
        {isSubmitting ? 'Registrando…' : 'Confirmar resolución'}
      </button>
    </form>
  )
}

export default AdminResolutionForm
