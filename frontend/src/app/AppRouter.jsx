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

function AppRouter() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route element={<MainLayout />}>
            <Route index element={<HomePage />} />
            <Route path="acceso" element={<AccessPage />} />
            <Route path="panel" element={<ProtectedRoute><DashboardPage /></ProtectedRoute>} />
            <Route path="cotizar/auto" element={<ProtectedRoute><AutoQuotePage /></ProtectedRoute>} />
            <Route path="cotizar/medico" element={<ProtectedRoute><MedicalQuotePage /></ProtectedRoute>} />
            <Route path="cotizaciones/:quoteId" element={<ProtectedRoute><QuoteResultPage /></ProtectedRoute>} />
            <Route path="contratacion/pago" element={<ProtectedRoute><PaymentPage /></ProtectedRoute>} />
            <Route path="polizas/:polizaId" element={<PolicyPage />} />
            <Route path="polizas/:polizaId/cancelacion" element={<CancellationPage />} />
            <Route path="*" element={<NotFoundPage />} />
          </Route>
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  )
}

export default AppRouter
