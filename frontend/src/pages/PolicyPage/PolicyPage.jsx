import { useState } from 'react'
import { Link, useLocation, useParams } from 'react-router-dom'
import DocumentList from '../../components/policy/DocumentList/DocumentList.jsx'
import PaymentOverview from '../../components/policy/PaymentOverview/PaymentOverview.jsx'
import PolicyDetails from '../../components/policy/PolicyDetails/PolicyDetails.jsx'
import StatusBadge from '../../components/ui/StatusBadge/StatusBadge.jsx'
import { policyData } from '../../mocks/policyData.js'
import NotFoundPage from '../NotFoundPage/NotFoundPage.jsx'
import './PolicyPage.css'

function PolicyPage() {
  const { polizaId } = useParams()
  const location = useLocation()
  const [downloadMessage, setDownloadMessage] = useState('')
  const policy = location.state?.policy ?? policyData[polizaId]
  const issuedAfterPayment = ['APPROVED', 'APROBADO'].includes(location.state?.payment?.status)
  const cancellationRequested = location.state?.cancellationStatus === 'CANCELLATION_REQUESTED'

  if (!policy) {
    return <NotFoundPage title="Póliza no encontrada" message="No existe una póliza accesible con ese identificador." backTo="/panel" backLabel="Volver al panel" />
  }

  function handleDownload(document) {
    setDownloadMessage(`Descarga simulada: ${document.name}. El PDF real se generará desde el backend.`)
  }

  return (
    <div className="policy-page page-container">
      <header className="policy-page__heading">
        <p className="eyebrow">Póliza {policy.policyNumber}</p>
        <div className="policy-page__title-row">
          <h1>{cancellationRequested ? 'Cancelación en revisión' : 'Tu póliza está activa'}</h1>
          <StatusBadge tone={cancellationRequested ? 'warning' : 'success'}>{cancellationRequested ? 'CANCELACIÓN SOLICITADA' : policy.status}</StatusBadge>
        </div>
        <p>Cobertura vigente desde el {policy.validFrom}.</p>
      </header>

      {issuedAfterPayment && <p className="confirmation-message" role="status">Pago aprobado. La póliza se emitió correctamente.</p>}
      {cancellationRequested && <p className="prototype-notice" role="status">La solicitud está en revisión. La cobertura permanece activa.</p>}

      <div className="policy-page__cards">
        <PolicyDetails policy={policy} />
        <PaymentOverview policy={policy} />
      </div>

      <DocumentList documents={policy.documents} onDownload={handleDownload} />
      {downloadMessage && <p className="prototype-notice" role="status">{downloadMessage}</p>}

      <div className="policy-page__actions">
        <Link className="button button--secondary" to="/panel">Volver al panel</Link>
        {!cancellationRequested && (
          <Link className="button button--primary" to={`/polizas/${polizaId}/cancelacion`} state={{ policy }}>Solicitar cancelación</Link>
        )}
      </div>
    </div>
  )
}

export default PolicyPage
