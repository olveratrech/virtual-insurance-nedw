import { useState } from 'react'
import FormField from '../../ui/FormField/FormField.jsx'
import '../../auth/authForms.css'

function AdminLoginForm({ onSuccess }) {
  const [values, setValues] = useState({ email: '', password: '' })
  const [errors, setErrors] = useState({})
  const [submitError, setSubmitError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  function updateField(event) {
    const { name, value } = event.target
    setValues((current) => ({ ...current, [name]: value }))
  }

  async function handleSubmit(event) {
    event.preventDefault()
    const nextErrors = {}
    if (!values.email.trim()) nextErrors.email = 'Ingresa tu correo institucional.'
    if (!values.password) nextErrors.password = 'Ingresa tu contraseña.'
    setErrors(nextErrors)
    setSubmitError('')
    if (Object.keys(nextErrors).length) return

    setIsSubmitting(true)
    try {
      await onSuccess(values)
    } catch (error) {
      setSubmitError(error.message)
      setValues((current) => ({ ...current, password: '' }))
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <form className="auth-form" onSubmit={handleSubmit} noValidate>
      <div>
        <h2>Iniciar sesión</h2>
        <p>Acceso exclusivo para personal autorizado.</p>
      </div>
      <FormField id="admin-email" label="Correo institucional" name="email" type="email" autoComplete="username" value={values.email} onChange={updateField} error={errors.email} />
      <FormField id="admin-password" label="Contraseña" name="password" type="password" autoComplete="current-password" value={values.password} onChange={updateField} error={errors.password} />
      {submitError && <p className="auth-form__error" role="alert">{submitError}</p>}
      <button className="button button--primary auth-form__submit" type="submit" disabled={isSubmitting}>
        {isSubmitting ? 'Ingresando…' : 'Entrar al panel'}
      </button>
    </form>
  )
}

export default AdminLoginForm
