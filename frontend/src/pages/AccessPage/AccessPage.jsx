import { useState } from 'react'
import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import LoginForm from '../../components/auth/LoginForm/LoginForm.jsx'
import RegisterForm from '../../components/auth/RegisterForm/RegisterForm.jsx'
import useAuth from '../../hooks/useAuth.js'
import './AccessPage.css'

function AccessPage() {
  const location = useLocation()
  const navigate = useNavigate()
  const { client, login, register, sessionStatus } = useAuth()
  const [mode, setMode] = useState('login')
  const authenticatedDestination = location.state?.from?.pathname ?? '/panel'

  async function handleLogin(credentials) {
    await login(credentials)
    navigate(authenticatedDestination, {
      replace: true,
      state: { message: 'Inicio de sesión correcto.' },
    })
  }

  async function handleRegister(data, photo) {
    await register(data, photo)
    navigate('/panel', {
      replace: true,
      state: { message: 'Cuenta creada correctamente.' },
    })
  }

  if (sessionStatus === 'authenticated' && client) {
    return <Navigate to={authenticatedDestination} replace />
  }

  return (
    <div className="access-page">
      <section className="access-page__institutional">
        <div>
          <p className="eyebrow">Portal de asegurados</p>
          <h1>Tu protección, administrada con claridad.</h1>
          <p>Consulta propuestas, pagos y documentos de tus pólizas desde un entorno seguro y centralizado.</p>
        </div>
        <ul aria-label="Beneficios del portal">
          <li>Información de cuenta protegida</li>
          <li>Seguimiento integral de tus operaciones</li>
          <li>Documentación disponible en todo momento</li>
        </ul>
      </section>
      <section className="access-page__panel">
        <div className="access-page__panel-inner">
          <header>
            <p className="eyebrow">Acceso seguro</p>
            <h2>{mode === 'login' ? 'Bienvenido de nuevo' : 'Crea tu cuenta'}</h2>
            <p>{mode === 'login' ? 'Ingresa tus credenciales para continuar.' : 'Completa los datos de identificación solicitados.'}</p>
          </header>
          <div className="access-page__tabs" role="tablist" aria-label="Opciones de acceso">
            <button type="button" role="tab" aria-selected={mode === 'login'} onClick={() => setMode('login')}>Iniciar sesión</button>
            <button type="button" role="tab" aria-selected={mode === 'register'} onClick={() => setMode('register')}>Registro</button>
          </div>
          {mode === 'login' ? <LoginForm onSuccess={handleLogin} /> : <RegisterForm onSuccess={handleRegister} />}
          <aside className="access-page__recovery">
            <strong>¿No puedes acceder?</strong>
            <span>La recuperación autogestionada se habilitará cuando el servicio seguro de restablecimiento esté disponible.</span>
          </aside>
        </div>
      </section>
    </div>
  )
}

export default AccessPage
