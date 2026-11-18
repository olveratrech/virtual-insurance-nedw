import { BrowserRouter, Route, Routes } from 'react-router-dom'
import MainLayout from '../layouts/MainLayout.jsx'
import AccessPage from '../pages/AccessPage/AccessPage.jsx'
import ComingSoonPage from '../pages/ComingSoonPage/ComingSoonPage.jsx'
import HomePage from '../pages/HomePage/HomePage.jsx'

function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<MainLayout />}>
          <Route index element={<HomePage />} />
          <Route path="acceso" element={<AccessPage />} />
          <Route path="panel" element={<ComingSoonPage title="Panel del cliente" />} />
          <Route path="cotizar/auto" element={<ComingSoonPage title="Cotización de seguro de auto" />} />
          <Route path="cotizar/medico" element={<ComingSoonPage title="Cotización de seguro médico" />} />
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
