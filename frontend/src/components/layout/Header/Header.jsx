import { useState } from 'react'
import { Link, NavLink, useNavigate } from 'react-router-dom'
import useAuth from '../../../hooks/useAuth.js'
import './Header.css'

function Header() {
  const navigate = useNavigate()
  const { client, logout, sessionStatus } = useAuth()
  const [logoutError, setLogoutError] = useState('')
  const [isLoggingOut, setIsLoggingOut] = useState(false)

  async function handleLogout() {
    setLogoutError('')
    setIsLoggingOut(true)

    try {
      await logout()
      navigate('/', { replace: true })
    } catch (error) {
      setLogoutError(error.message)
    } finally {
      setIsLoggingOut(false)
    }
  }

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
        <div className="site-header__session">
          {sessionStatus === 'authenticated' && client ? (
            <>
              <span className="site-header__customer">Hola, {client.name}</span>
              <button
                className="button button--secondary"
                type="button"
                disabled={isLoggingOut}
                onClick={handleLogout}
              >
                {isLoggingOut ? 'Cerrando…' : 'Cerrar sesión'}
              </button>
            </>
          ) : (
            <Link className="button button--secondary" to="/acceso">Iniciar sesión</Link>
          )}
          {logoutError && <span className="site-header__error" role="alert">{logoutError}</span>}
        </div>
      </div>
    </header>
  )
}

export default Header
