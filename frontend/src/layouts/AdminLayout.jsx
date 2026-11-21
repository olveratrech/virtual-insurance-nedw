import { Outlet } from 'react-router-dom'
import AdminHeader from '../components/admin/AdminHeader/AdminHeader.jsx'
import DisclosureFooter from '../components/layout/DisclosureFooter/DisclosureFooter.jsx'

function AdminLayout() {
  return (
    <div className="app-shell admin-shell">
      <AdminHeader />
      <main><Outlet /></main>
      <DisclosureFooter />
    </div>
  )
}

export default AdminLayout
