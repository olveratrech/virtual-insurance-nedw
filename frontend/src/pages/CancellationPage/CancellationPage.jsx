import { useState } from 'react'
import { Link, useLocation, useParams } from 'react-router-dom'
import CancellationForm from '../../components/cancellation/CancellationForm/CancellationForm.jsx'
import StatusBadge from '../../components/ui/StatusBadge/StatusBadge.jsx'
import { createCancellationRequest } from '../../mocks/cancellationRules.js'
import { policyData } from '../../mocks/policyData.js'
import './CancellationPage.css'

function CancellationPage() {
  const { polizaId } = useParams()
  const location = useLocation()
  const [request, setRequest] = useState(null)
  const policy = location.state?.policy ?? policyData[polizaId] ?? policyData['802145']

  function handleSubmitRequest(values) {
    setRequest(createCancellationRequest(values))
  }

  return (
    <div className="cancellation-page page-container">
      <header className="cancellation-page__heading">
        <p className="eyebrow">Gestión de póliza</p>
        <h1>Solicitar cancelación</h1>
        <p>{policy.policyNumber} · Estado actual: {policy.status}</p>
      </header>

      {request ? (
        <section className="cancellation-confirmation" aria-labelledby="cancellation-confirmation-title">
          <StatusBadge tone="warning">CANCELACIÓN SOLICITADA</StatusBadge>
          <h2 id="cancellation-confirmation-title">Recibimos tu solicitud</h2>
          <p>Folio: <strong>{request.requestNumber}</strong></p>
          <p>La póliza permanece activa mientras se revisa la solicitud. No se ha efectuado devolución ni cambio de cobertura.</p>
          <div className="cancellation-confirmation__actions">
            <Link className="button button--primary" to={`/polizas/${polizaId}`} state={{ cancellationStatus: request.status }}>Volver a la póliza</Link>
            <Link className="button button--secondary" to="/panel">Ir al panel</Link>
          </div>
        </section>
      ) : (
        <CancellationForm policyId={polizaId} onSubmitRequest={handleSubmitRequest} />
      )}
    </div>
  )
}

export default CancellationPage
