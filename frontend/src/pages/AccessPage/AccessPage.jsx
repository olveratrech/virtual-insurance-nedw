import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import LoginForm from '../../components/auth/LoginForm/LoginForm.jsx'
import RegisterForm from '../../components/auth/RegisterForm/RegisterForm.jsx'
import useAuth from '../../hooks/useAuth.js'
import './AccessPage.css'

function AccessPage() {
  const location = useLocation()
  const navigate = useNavigate()
  const { client, login, register, sessionStatus } = useAuth()
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
    <div className="access-page page-container">
      <header className="access-page__heading">
        <p className="eyebrow">Acceso seguro</p>
        <h1>Bienvenido a Virtual Insurance</h1>
        <p>Inicia sesión o crea una cuenta para guardar cotizaciones y consultar pólizas.</p>
      </header>
      <div className="access-page__forms">
        <LoginForm onSuccess={handleLogin} />
        <RegisterForm onSuccess={handleRegister} />
      </div>
    </div>
  )
}

export default AccessPage
