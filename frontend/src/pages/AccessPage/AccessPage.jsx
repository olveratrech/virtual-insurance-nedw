import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import LoginForm from '../../components/auth/LoginForm/LoginForm.jsx'
import RegisterForm from '../../components/auth/RegisterForm/RegisterForm.jsx'
import useAuth from '../../hooks/useAuth.js'
import './AccessPage.css'

function AccessPage() {
  const location = useLocation()
  const navigate = useNavigate()
  const { client, login, sessionStatus } = useAuth()

  async function handleLogin(credentials) {
    await login(credentials)
    const destination = location.state?.from?.pathname ?? '/panel'
    navigate(destination, {
      replace: true,
      state: { message: 'Inicio de sesión correcto.' },
    })
  }

  if (sessionStatus === 'authenticated' && client) return <Navigate to="/panel" replace />

  return (
    <div className="access-page page-container">
      <header className="access-page__heading">
        <p className="eyebrow">Acceso seguro</p>
        <h1>Bienvenido a Virtual Insurance</h1>
        <p>Inicia sesión o crea una cuenta para guardar cotizaciones y consultar pólizas.</p>
      </header>

      <aside className="prototype-notice" role="note">
        El inicio de sesión ya utiliza Spring Security y Oracle. El registro se actualizará en el siguiente incremento.
      </aside>

      <div className="access-page__forms">
        <LoginForm onSuccess={handleLogin} />
        <RegisterForm />
      </div>
    </div>
  )
}

export default AccessPage
