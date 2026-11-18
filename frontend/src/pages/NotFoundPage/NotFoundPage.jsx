import { Link, useLocation } from 'react-router-dom'
import './NotFoundPage.css'

function NotFoundPage({
  title = 'Página no encontrada',
  message = 'La dirección solicitada no existe o ya no está disponible.',
  backTo = '/',
  backLabel = 'Volver al inicio',
}) {
  const location = useLocation()

  return (
    <div className="not-found-page page-container">
      <p className="eyebrow">Error 404</p>
      <h1>{title}</h1>
      <p>{message}</p>
      <code>{location.pathname}</code>
      <Link className="button button--primary" to={backTo}>{backLabel}</Link>
    </div>
  )
}

export default NotFoundPage
