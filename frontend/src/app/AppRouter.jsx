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
import AccountHistoryPage from '../pages/AccountHistoryPage/AccountHistoryPage.jsx'
import QuoteHubPage from '../pages/QuoteHubPage/QuoteHubPage.jsx'
import HelpPage from '../pages/HelpPage/HelpPage.jsx'
import ProfilePage from '../pages/ProfilePage/ProfilePage.jsx'
import LegalInformationPage from '../pages/LegalInformationPage/LegalInformationPage.jsx'

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
          <Route path="condiciones-generales" element={<LegalInformationPage type="conditions" />} />
          <Route path="aviso-privacidad" element={<LegalInformationPage type="privacy" />} />
          <Route path="panel" element={<ProtectedRoute><DashboardPage /></ProtectedRoute>} />
          <Route path="cotizar" element={<ProtectedRoute><QuoteHubPage /></ProtectedRoute>} />
          <Route path="cotizar/auto" element={<ProtectedRoute><AutoQuotePage /></ProtectedRoute>} />
          <Route path="cotizar/medico" element={<ProtectedRoute><MedicalQuotePage /></ProtectedRoute>} />
          <Route path="cotizaciones" element={<ProtectedRoute><AccountHistoryPage key="quotes" eyebrow="Mi cuenta" title="Cotizaciones" description="Consulta todas las propuestas que has solicitado." historyKey="quotes" emptyMessage="Todavía no has solicitado ninguna cotización." /></ProtectedRoute>} />
          <Route path="cotizaciones/:quoteId" element={<ProtectedRoute><QuoteResultPage /></ProtectedRoute>} />
          <Route path="cotizaciones/:quoteId/pago" element={<ProtectedRoute><PaymentPage /></ProtectedRoute>} />
          <Route path="polizas" element={<ProtectedRoute><AccountHistoryPage key="policies" eyebrow="Mi cuenta" title="Pólizas" description="Revisa tus coberturas emitidas y su estado actual." historyKey="policies" emptyMessage="Todavía no tienes pólizas emitidas." /></ProtectedRoute>} />
          <Route path="polizas/:polizaId" element={<ProtectedRoute><PolicyPage /></ProtectedRoute>} />
          <Route path="polizas/:polizaId/cancelacion" element={<ProtectedRoute><CancellationPage /></ProtectedRoute>} />
          <Route path="pagos" element={<ProtectedRoute><AccountHistoryPage key="payments" eyebrow="Mi cuenta" title="Pagos" description="Consulta todos los intentos de pago registrados en tu cuenta." historyKey="payments" emptyMessage="Todavía no tienes intentos de pago registrados." /></ProtectedRoute>} />
          <Route path="ayuda" element={<ProtectedRoute><HelpPage /></ProtectedRoute>} />
          <Route path="perfil" element={<ProtectedRoute><ProfilePage /></ProtectedRoute>} />
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
