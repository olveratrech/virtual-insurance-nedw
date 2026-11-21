import { useContext } from 'react'
import AdminAuthContext from '../contexts/adminAuthContext.js'

export default function useAdminAuth() {
  const context = useContext(AdminAuthContext)
  if (!context) throw new Error('useAdminAuth debe utilizarse dentro de AdminAuthProvider.')
  return context
}
