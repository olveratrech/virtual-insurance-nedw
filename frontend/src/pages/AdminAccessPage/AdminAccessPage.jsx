import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import AdminLoginForm from '../../components/admin/AdminLoginForm/AdminLoginForm.jsx'
import useAdminAuth from '../../hooks/useAdminAuth.js'
import './AdminAccessPage.css'

function AdminAccessPage() {
  const location = useLocation()
  const navigate = useNavigate()
  const { user, login, sessionStatus } = useAdminAuth()
  const destination = location.state?.from?.pathname ?? '/admin/cancelaciones'

  async function handleLogin(credentials) {
    await login(credentials)
    navigate(destination, { replace: true })
  }

  if (sessionStatus === 'authenticated' && user) return <Navigate to={destination} replace />

  return (
    <div className="admin-access page-container">
      <header>
        <p className="eyebrow">Operación interna</p>
        <h1>Panel administrativo</h1>
        <p>Revisa y resuelve solicitudes de cancelación con trazabilidad en Oracle.</p>
      </header>
      <div className="admin-access__content">
        <AdminLoginForm onSuccess={handleLogin} />
        <aside className="admin-access__notice" role="note">
          <strong>Sesión independiente por identidad</strong>
          <p>Al ingresar aquí se reemplazará cualquier sesión de cliente abierta en este navegador.</p>
        </aside>
      </div>
    </div>
  )
}

export default AdminAccessPage
