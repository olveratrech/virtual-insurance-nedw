import { BrowserRouter, Route, Routes } from 'react-router-dom'
import MainLayout from '../layouts/MainLayout.jsx'
import AccessPage from '../pages/AccessPage/AccessPage.jsx'
import DashboardPage from '../pages/DashboardPage/DashboardPage.jsx'
import HomePage from '../pages/HomePage/HomePage.jsx'
import AutoQuotePage from '../pages/AutoQuotePage/AutoQuotePage.jsx'
import MedicalQuotePage from '../pages/MedicalQuotePage/MedicalQuotePage.jsx'
import QuoteResultPage from '../pages/QuoteResultPage/QuoteResultPage.jsx'
import PaymentPage from '../pages/PaymentPage/PaymentPage.jsx'
import PolicyPage from '../pages/PolicyPage/PolicyPage.jsx'
import CancellationPage from '../pages/CancellationPage/CancellationPage.jsx'
import NotFoundPage from '../pages/NotFoundPage/NotFoundPage.jsx'
import ProtectedRoute from '../components/auth/ProtectedRoute/ProtectedRoute.jsx'
import { AuthProvider } from '../contexts/AuthContext.jsx'
import { AdminAuthProvider } from '../contexts/AdminAuthContext.jsx'
import AdminLayout from '../layouts/AdminLayout.jsx'
import AdminAccessPage from '../pages/AdminAccessPage/AdminAccessPage.jsx'
import AdminCancellationsPage from '../pages/AdminCancellationsPage/AdminCancellationsPage.jsx'
import AdminCancellationDetailPage from '../pages/AdminCancellationDetailPage/AdminCancellationDetailPage.jsx'
import AdminProtectedRoute from '../components/admin/AdminProtectedRoute/AdminProtectedRoute.jsx'

function ClientApplication() {
  return <AuthProvider><MainLayout /></AuthProvider>
}

function AdminApplication() {
  return <AdminAuthProvider><AdminLayout /></AdminAuthProvider>
}

function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<ClientApplication />}>
          <Route index element={<HomePage />} />
          <Route path="acceso" element={<AccessPage />} />
          <Route path="panel" element={<ProtectedRoute><DashboardPage /></ProtectedRoute>} />
          <Route path="cotizar/auto" element={<ProtectedRoute><AutoQuotePage /></ProtectedRoute>} />
          <Route path="cotizar/medico" element={<ProtectedRoute><MedicalQuotePage /></ProtectedRoute>} />
          <Route path="cotizaciones/:quoteId" element={<ProtectedRoute><QuoteResultPage /></ProtectedRoute>} />
          <Route path="cotizaciones/:quoteId/pago" element={<ProtectedRoute><PaymentPage /></ProtectedRoute>} />
          <Route path="polizas/:polizaId" element={<ProtectedRoute><PolicyPage /></ProtectedRoute>} />
          <Route path="polizas/:polizaId/cancelacion" element={<ProtectedRoute><CancellationPage /></ProtectedRoute>} />
          <Route path="*" element={<NotFoundPage />} />
        </Route>
        <Route path="admin" element={<AdminApplication />}>
          <Route path="acceso" element={<AdminAccessPage />} />
          <Route path="cancelaciones" element={<AdminProtectedRoute><AdminCancellationsPage /></AdminProtectedRoute>} />
          <Route path="cancelaciones/:cancellationId" element={<AdminProtectedRoute><AdminCancellationDetailPage /></AdminProtectedRoute>} />
          <Route index element={<AdminProtectedRoute><AdminCancellationsPage /></AdminProtectedRoute>} />
          <Route path="*" element={<NotFoundPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default AppRouter
