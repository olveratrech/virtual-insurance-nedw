import { BrowserRouter, Route, Routes } from 'react-router-dom'
import MainLayout from '../layouts/MainLayout.jsx'
import AccessPage from '../pages/AccessPage/AccessPage.jsx'
import ComingSoonPage from '../pages/ComingSoonPage/ComingSoonPage.jsx'
import DashboardPage from '../pages/DashboardPage/DashboardPage.jsx'
import HomePage from '../pages/HomePage/HomePage.jsx'
import AutoQuotePage from '../pages/AutoQuotePage/AutoQuotePage.jsx'
import MedicalQuotePage from '../pages/MedicalQuotePage/MedicalQuotePage.jsx'

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
          <Route path="cotizaciones/aprobada" element={<ComingSoonPage title="Cotización aprobada" />} />
          <Route path="cotizaciones/rechazada" element={<ComingSoonPage title="Cotización rechazada" />} />
          <Route path="contratacion/pago" element={<ComingSoonPage title="Contratación y pago" />} />
          <Route path="polizas/:polizaId" element={<ComingSoonPage title="Detalle de póliza" />} />
          <Route path="polizas/:polizaId/cancelacion" element={<ComingSoonPage title="Solicitud de cancelación" />} />
          <Route path="*" element={<ComingSoonPage title="Página no encontrada" />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default AppRouter
