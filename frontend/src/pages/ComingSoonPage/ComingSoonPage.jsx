import { Link, useLocation } from 'react-router-dom'
import './ComingSoonPage.css'

function ComingSoonPage({ title }) {
  const location = useLocation()

  return (
    <div className="coming-soon page-container">
      <p className="eyebrow">Pantalla pendiente</p>
      <h1>{title}</h1>
      <p>La ruta ya está preparada, pero esta pantalla todavía no se ha implementado.</p>
      <code>{location.pathname}</code>
      <Link className="button button--primary" to="/">Volver al inicio</Link>
    </div>
  )
}

export default ComingSoonPage
