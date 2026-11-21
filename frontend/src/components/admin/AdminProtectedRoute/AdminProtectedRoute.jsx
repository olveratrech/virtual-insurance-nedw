import { Navigate, useLocation } from 'react-router-dom'
import useAdminAuth from '../../../hooks/useAdminAuth.js'

function AdminProtectedRoute({ children }) {
  const location = useLocation()
  const { user, refreshSession, sessionStatus } = useAdminAuth()

  if (sessionStatus === 'checking') {
    return <p className="page-container route-status" role="status">Comprobando la sesión administrativa…</p>
  }

  if (sessionStatus === 'error') {
    return (
      <section className="page-container route-status" role="alert">
        <h1>No fue posible comprobar la sesión administrativa</h1>
        <p>Verifica que el servidor esté disponible e inténtalo nuevamente.</p>
        <button className="button button--primary" type="button" onClick={refreshSession}>Reintentar</button>
      </section>
    )
  }

  if (!user) return <Navigate to="/admin/acceso" replace state={{ from: location }} />
  return children
}

export default AdminProtectedRoute
