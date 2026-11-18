import { Outlet } from 'react-router-dom'
import Header from '../components/layout/Header/Header.jsx'

function MainLayout() {
  return (
    <div className="app-shell">
      <Header />
      <main>
        <Outlet />
      </main>
    </div>
  )
}

export default MainLayout
