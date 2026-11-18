import { useState } from 'react'
import FormField from '../../ui/FormField/FormField.jsx'
import '../authForms.css'

const initialValues = {
  name: '',
  email: '',
  password: '',
  passwordConfirmation: '',
  acceptsTerms: false,
}

function RegisterForm({ onSuccess }) {
  const [values, setValues] = useState(initialValues)
  const [errors, setErrors] = useState({})

  function updateField(event) {
    const { name, type, checked, value } = event.target
    setValues((currentValues) => ({
      ...currentValues,
      [name]: type === 'checkbox' ? checked : value,
    }))
  }

  function handleSubmit(event) {
    event.preventDefault()

    const nextErrors = {}
    if (!values.name.trim()) nextErrors.name = 'Ingresa tu nombre completo.'
    if (!values.email.trim()) nextErrors.email = 'Ingresa tu correo electrónico.'
    if (values.password.length < 8) nextErrors.password = 'Utiliza al menos 8 caracteres.'
    if (values.passwordConfirmation !== values.password) {
      nextErrors.passwordConfirmation = 'Las contraseñas no coinciden.'
    }
    if (!values.acceptsTerms) nextErrors.acceptsTerms = 'Debes aceptar los términos para continuar.'

    setErrors(nextErrors)
    if (Object.keys(nextErrors).length === 0) onSuccess('register')
  }

  return (
    <form className="auth-form" onSubmit={handleSubmit} noValidate>
      <div>
        <h2>Crear cuenta</h2>
        <p>Guarda cotizaciones y administra tus seguros.</p>
      </div>

      <FormField id="register-name" label="Nombre completo" name="name" autoComplete="name" value={values.name} onChange={updateField} error={errors.name} />
      <FormField id="register-email" label="Correo electrónico" name="email" type="email" autoComplete="email" value={values.email} onChange={updateField} error={errors.email} />
      <FormField id="register-password" label="Contraseña" name="password" type="password" autoComplete="new-password" value={values.password} onChange={updateField} error={errors.password} />
      <FormField id="register-password-confirmation" label="Confirmar contraseña" name="passwordConfirmation" type="password" autoComplete="new-password" value={values.passwordConfirmation} onChange={updateField} error={errors.passwordConfirmation} />

      <div className="checkbox-field">
        <input id="accepts-terms" name="acceptsTerms" type="checkbox" checked={values.acceptsTerms} onChange={updateField} />
        <label htmlFor="accepts-terms">Acepto los términos, condiciones y aviso de privacidad.</label>
      </div>
      {errors.acceptsTerms && <p className="form-field__error">{errors.acceptsTerms}</p>}

      <button className="button button--primary auth-form__submit" type="submit">
        Crear cuenta
      </button>
    </form>
  )
}

export default RegisterForm
