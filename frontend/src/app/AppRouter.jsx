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

function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<MainLayout />}>
          <Route index element={<HomePage />} />
          <Route path="acceso" element={<AccessPage />} />
          <Route path="panel" element={<DashboardPage />} />
          <Route path="cotizar/auto" element={<AutoQuotePage />} />
          <Route path="cotizar/medico" element={<MedicalQuotePage />} />
          <Route path="cotizaciones/aprobada" element={<QuoteResultPage outcome="approved" />} />
          <Route path="cotizaciones/rechazada" element={<QuoteResultPage outcome="rejected" />} />
          <Route path="contratacion/pago" element={<PaymentPage />} />
          <Route path="polizas/:polizaId" element={<PolicyPage />} />
          <Route path="polizas/:polizaId/cancelacion" element={<CancellationPage />} />
          <Route path="*" element={<NotFoundPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default AppRouter
