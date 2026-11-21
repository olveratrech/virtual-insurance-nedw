import { Outlet } from 'react-router-dom'
import AdminHeader from '../components/admin/AdminHeader/AdminHeader.jsx'

function AdminLayout() {
  return (
    <div className="app-shell admin-shell">
      <AdminHeader />
      <main><Outlet /></main>
    </div>
  )
}

export default AdminLayout
