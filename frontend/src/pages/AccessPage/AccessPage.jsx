import { useNavigate } from 'react-router-dom'
import LoginForm from '../../components/auth/LoginForm/LoginForm.jsx'
import RegisterForm from '../../components/auth/RegisterForm/RegisterForm.jsx'
import './AccessPage.css'

function AccessPage() {
  const navigate = useNavigate()

  function handleAuthenticationSuccess(operation) {
    navigate('/panel', {
      state: {
        message: operation === 'login'
          ? 'Inicio de sesión simulado correctamente.'
          : 'Registro simulado correctamente.',
      },
    })
  }

  return (
    <div className="access-page page-container">
      <header className="access-page__heading">
        <p className="eyebrow">Acceso seguro</p>
        <h1>Bienvenido a Virtual Insurance</h1>
        <p>Inicia sesión o crea una cuenta para guardar cotizaciones y consultar pólizas.</p>
      </header>

      <aside className="prototype-notice" role="note">
        Esta versión utiliza datos simulados. La autenticación real se conectará posteriormente con Spring Security.
      </aside>

      <div className="access-page__forms">
        <LoginForm onSuccess={handleAuthenticationSuccess} />
        <RegisterForm onSuccess={handleAuthenticationSuccess} />
      </div>
    </div>
  )
}

export default AccessPage
