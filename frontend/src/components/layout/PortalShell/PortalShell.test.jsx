import { fireEvent, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import AuthContext from '../../../contexts/authContext.js'
import PortalShell from './PortalShell.jsx'

function renderShell() {
  const logout = vi.fn().mockResolvedValue(undefined)
  render(
    <AuthContext.Provider value={{ client: { name: 'Emmanuel', paternalSurname: 'Trejo' }, logout }}>
      <MemoryRouter initialEntries={['/panel']}>
        <PortalShell footer={<footer>Pie institucional</footer>}><h1>Resumen</h1></PortalShell>
      </MemoryRouter>
    </AuthContext.Provider>,
  )
  return { logout }
}

describe('PortalShell', () => {
  it('renderiza la navegación de escritorio y la topbar móvil con marcas oficiales', () => {
    renderShell()
    expect(screen.getAllByAltText('Virtual Insurance')).toHaveLength(3)
    expect(screen.getAllByRole('navigation', { name: 'Navegación de la cuenta', hidden: true })).toHaveLength(2)
    expect(screen.getByRole('button', { name: 'Abrir menú de navegación', hidden: true })).toHaveAttribute('aria-expanded', 'false')
    expect(screen.getByText('Pie institucional')).toBeInTheDocument()
  })

  it('abre y cierra el drawer desde el botón y el fondo', () => {
    renderShell()
    const menuButton = screen.getByRole('button', { name: 'Abrir menú de navegación', hidden: true })
    fireEvent.click(menuButton)
    expect(menuButton).toHaveAttribute('aria-expanded', 'true')
    expect(document.querySelector('#portal-drawer')).toHaveClass('portal-shell__drawer--open')
    fireEvent.click(screen.getAllByRole('button', { name: 'Cerrar menú', hidden: true })[0])
    expect(menuButton).toHaveAttribute('aria-expanded', 'false')
  })

  it('cierra el drawer con Escape y al seleccionar una ruta', () => {
    renderShell()
    const menuButton = screen.getByRole('button', { name: 'Abrir menú de navegación', hidden: true })
    fireEvent.click(menuButton)
    fireEvent.keyDown(document, { key: 'Escape' })
    expect(menuButton).toHaveAttribute('aria-expanded', 'false')

    fireEvent.click(menuButton)
    const policyLinks = screen.getAllByRole('link', { name: 'Mis pólizas', hidden: true })
    fireEvent.click(policyLinks.at(-1))
    expect(menuButton).toHaveAttribute('aria-expanded', 'false')
  })
})
