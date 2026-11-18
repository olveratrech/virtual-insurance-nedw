import { useState } from 'react'
import FormField from '../../ui/FormField/FormField.jsx'
import '../authForms.css'

function LoginForm({ onSuccess }) {
  const [values, setValues] = useState({ email: '', password: '' })
  const [errors, setErrors] = useState({})

  function updateField(event) {
    const { name, value } = event.target
    setValues((currentValues) => ({ ...currentValues, [name]: value }))
  }

  function handleSubmit(event) {
    event.preventDefault()

    const nextErrors = {}
    if (!values.email.trim()) nextErrors.email = 'Ingresa tu correo electrónico.'
    if (!values.password) nextErrors.password = 'Ingresa tu contraseña.'

    setErrors(nextErrors)
    if (Object.keys(nextErrors).length === 0) onSuccess('login')
  }

  return (
    <form className="auth-form" onSubmit={handleSubmit} noValidate>
      <div>
        <h2>Iniciar sesión</h2>
        <p>Consulta tus cotizaciones, pagos y pólizas.</p>
      </div>

      <FormField
        id="login-email"
        label="Correo electrónico"
        name="email"
        type="email"
        autoComplete="email"
        value={values.email}
        onChange={updateField}
        error={errors.email}
      />
      <FormField
        id="login-password"
        label="Contraseña"
        name="password"
        type="password"
        autoComplete="current-password"
        value={values.password}
        onChange={updateField}
        error={errors.password}
      />

      <button className="button button--primary auth-form__submit" type="submit">
        Entrar
      </button>
    </form>
  )
}

export default LoginForm
