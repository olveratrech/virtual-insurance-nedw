import { useState } from 'react'
import { Link, NavLink, useNavigate } from 'react-router-dom'
import useAdminAuth from '../../../hooks/useAdminAuth.js'
import './AdminHeader.css'

function AdminHeader() {
  const navigate = useNavigate()
  const { user, logout, sessionStatus } = useAdminAuth()
  const [isLoggingOut, setIsLoggingOut] = useState(false)
  const [logoutError, setLogoutError] = useState('')

  async function handleLogout() {
    setIsLoggingOut(true)
    setLogoutError('')
    try {
      await logout()
      navigate('/admin/acceso', { replace: true })
    } catch (error) {
      setLogoutError(error.message)
    } finally {
      setIsLoggingOut(false)
    }
  }

  return (
    <header className="admin-header">
      <div className="admin-header__content">
        <Link className="admin-header__brand" to="/admin/cancelaciones" aria-label="Virtual Insurance, administración">
          <span className="admin-header__brand-mark" aria-hidden="true">VI</span>
          <span><strong>Virtual Insurance</strong><small>Administración</small></span>
        </Link>
        {sessionStatus === 'authenticated' && user && (
          <>
            <nav className="admin-header__navigation" aria-label="Navegación administrativa">
              <NavLink to="/admin/cancelaciones">Cancelaciones</NavLink>
            </nav>
            <div className="admin-header__session">
              <span>{user.name}</span>
              <button className="button button--secondary" type="button" disabled={isLoggingOut} onClick={handleLogout}>
                {isLoggingOut ? 'Cerrando…' : 'Cerrar sesión'}
              </button>
              {logoutError && <span className="admin-header__error" role="alert">{logoutError}</span>}
            </div>
          </>
        )}
      </div>
    </header>
  )
}

export default AdminHeader
