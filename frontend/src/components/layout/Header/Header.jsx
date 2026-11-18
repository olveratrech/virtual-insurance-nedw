import { Link, NavLink } from 'react-router-dom'
import './Header.css'

function Header() {
  return (
    <header className="site-header">
      <div className="site-header__content">
        <Link className="site-header__brand" to="/" aria-label="Virtual Insurance, inicio">
          <span className="site-header__brand-mark" aria-hidden="true">VI</span>
          <span>Virtual Insurance</span>
        </Link>
        <nav className="site-header__navigation" aria-label="Navegación principal">
          <NavLink to="/" end>Inicio</NavLink>
          <NavLink to="/cotizar/auto">Cotizar</NavLink>
          <NavLink to="/panel">Mi cuenta</NavLink>
        </nav>
        <Link className="button button--secondary" to="/acceso">Iniciar sesión</Link>
      </div>
    </header>
  )
}

export default Header
