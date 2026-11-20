import { Navigate, useLocation } from 'react-router-dom'
import useAuth from '../../../hooks/useAuth.js'

function ProtectedRoute({ children }) {
  const location = useLocation()
  const { client, refreshSession, sessionStatus } = useAuth()

  if (sessionStatus === 'checking') {
    return <p className="page-container route-status" role="status">Comprobando tu sesión…</p>
  }

  if (sessionStatus === 'error') {
    return (
      <section className="page-container route-status" role="alert">
        <h1>No fue posible comprobar tu sesión</h1>
        <p>Verifica que el servidor esté disponible e inténtalo nuevamente.</p>
        <button className="button button--primary" type="button" onClick={refreshSession}>
          Reintentar
        </button>
      </section>
    )
  }

  if (!client) {
    return <Navigate to="/acceso" replace state={{ from: location }} />
  }

  return children
}

export default ProtectedRoute
