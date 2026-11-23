import { useEffect, useState } from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import brandLogo from '../../../assets/brand/virtual-insurance-logo.svg'
import brandMark from '../../../assets/brand/virtual-insurance-mark.svg'
import useAuth from '../../../hooks/useAuth.js'
import './PortalShell.css'

const navigation = [
  { to: '/panel', label: 'Resumen', icon: '⌂' },
  { to: '/polizas', label: 'Mis pólizas', icon: '▣' },
  { to: '/cotizaciones', label: 'Cotizaciones', icon: '☷' },
  { to: '/pagos', label: 'Pagos y facturación', icon: '$' },
  { to: '/perfil', label: 'Mi perfil', icon: '○' },
  { to: '/ayuda', label: 'Ayuda y siniestros', icon: '?' },
]

function PortalNavigation({ onNavigate, onLogout, isLoggingOut, logoutError }) {
  return (
    <>
      <NavLink className="portal-shell__brand" to="/panel" onClick={onNavigate}>
        <img src={brandLogo} alt="Virtual Insurance" />
      </NavLink>
      <NavLink className="portal-shell__quote-action" to="/cotizar" onClick={onNavigate}>
        <span aria-hidden="true">+</span> Nueva cotización
      </NavLink>
      <nav className="portal-shell__navigation" aria-label="Navegación de la cuenta">
        {navigation.map((item) => (
          <NavLink key={item.to} to={item.to} onClick={onNavigate}>
            <span className="portal-shell__nav-icon" aria-hidden="true">{item.icon}</span>
            <span>{item.label}</span>
          </NavLink>
        ))}
      </nav>
      <div className="portal-shell__session">
        <button type="button" onClick={onLogout} disabled={isLoggingOut}>
          {isLoggingOut ? 'Cerrando sesión…' : 'Cerrar sesión'}
        </button>
        {logoutError && <p role="alert">{logoutError}</p>}
      </div>
    </>
  )
}

function PortalShell({ children, footer }) {
  const navigate = useNavigate()
  const { client, logout } = useAuth()
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [isLoggingOut, setIsLoggingOut] = useState(false)
  const [logoutError, setLogoutError] = useState('')

  useEffect(() => {
    if (!drawerOpen) return undefined
    function closeOnEscape(event) {
      if (event.key === 'Escape') setDrawerOpen(false)
    }
    document.addEventListener('keydown', closeOnEscape)
    return () => document.removeEventListener('keydown', closeOnEscape)
  }, [drawerOpen])

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

  const navigationProps = {
    onNavigate: () => setDrawerOpen(false),
    onLogout: handleLogout,
    isLoggingOut,
    logoutError,
  }

  return (
    <div className="portal-shell">
      <aside className="portal-shell__sidebar"><PortalNavigation {...navigationProps} /></aside>
      <header className="portal-shell__mobile-header">
        <NavLink className="portal-shell__mobile-brand" to="/panel">
          <img src={brandMark} alt="Virtual Insurance" />
        </NavLink>
        <button
          className="portal-shell__menu-button"
          type="button"
          aria-label="Abrir menú de navegación"
          aria-expanded={drawerOpen}
          aria-controls="portal-drawer"
          onClick={() => setDrawerOpen(true)}
        >
          <i /><i /><i />
        </button>
      </header>
      {drawerOpen && <button className="portal-shell__backdrop" type="button" aria-label="Cerrar menú" onClick={() => setDrawerOpen(false)} />}
      <aside id="portal-drawer" className={`portal-shell__drawer${drawerOpen ? ' portal-shell__drawer--open' : ''}`} aria-hidden={!drawerOpen}>
        <div className="portal-shell__drawer-heading">
          <span>Menú</span>
          <button type="button" aria-label="Cerrar menú" onClick={() => setDrawerOpen(false)}>×</button>
        </div>
        <PortalNavigation {...navigationProps} />
      </aside>
      <div className="portal-shell__workspace">
        <div className="portal-shell__account-bar">
          <div><span>Portal de asegurado</span><strong>{client?.name} {client?.paternalSurname}</strong></div>
        </div>
        <main className="portal-shell__content">{children}</main>
        {footer}
      </div>
    </div>
  )
}

export default PortalShell
