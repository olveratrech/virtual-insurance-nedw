import { Outlet } from 'react-router-dom'
import DisclosureFooter from '../components/layout/DisclosureFooter/DisclosureFooter.jsx'
import Header from '../components/layout/Header/Header.jsx'

function MainLayout() {
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
