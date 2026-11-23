import { Outlet } from 'react-router-dom'
import DisclosureFooter from '../components/layout/DisclosureFooter/DisclosureFooter.jsx'
import Header from '../components/layout/Header/Header.jsx'
import PortalShell from '../components/layout/PortalShell/PortalShell.jsx'
import useAuth from '../hooks/useAuth.js'

function MainLayout() {
  const { sessionStatus } = useAuth()

  if (sessionStatus === 'authenticated') {
    return <PortalShell footer={<DisclosureFooter />}><Outlet /></PortalShell>
  }

  return (
    <div className="app-shell">
      <Header />
      <main>
        <Outlet />
      </main>
      <DisclosureFooter />
    </div>
  )
}

export default MainLayout
