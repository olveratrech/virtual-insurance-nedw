import { useState } from 'react'
import { Link, useLocation, useParams } from 'react-router-dom'
import DocumentList from '../../components/policy/DocumentList/DocumentList.jsx'
import PaymentOverview from '../../components/policy/PaymentOverview/PaymentOverview.jsx'
import PolicyDetails from '../../components/policy/PolicyDetails/PolicyDetails.jsx'
import StatusBadge from '../../components/ui/StatusBadge/StatusBadge.jsx'
import { policyData } from '../../mocks/policyData.js'
import './PolicyPage.css'

function PolicyPage() {
  const { polizaId } = useParams()
  const location = useLocation()
  const [downloadMessage, setDownloadMessage] = useState('')
  const policy = policyData[polizaId] ?? policyData['802145']
  const issuedAfterPayment = location.state?.payment?.status === 'APPROVED'

  function handleDownload(document) {
    setDownloadMessage(`Descarga simulada: ${document.name}. El PDF real se generará desde el backend.`)
  }

  return (
    <div className="policy-page page-container">
      <header className="policy-page__heading">
        <p className="eyebrow">Póliza {policy.policyNumber}</p>
        <div className="policy-page__title-row">
          <h1>Tu póliza está activa</h1>
          <StatusBadge tone="success">{policy.status}</StatusBadge>
        </div>
        <p>Cobertura vigente desde el {policy.validFrom}.</p>
      </header>

      {issuedAfterPayment && <p className="confirmation-message" role="status">Pago aprobado. La póliza se emitió correctamente.</p>}

      <div className="policy-page__cards">
        <PolicyDetails policy={policy} />
        <PaymentOverview policy={policy} />
      </div>

      <DocumentList documents={policy.documents} onDownload={handleDownload} />
      {downloadMessage && <p className="prototype-notice" role="status">{downloadMessage}</p>}

      <div className="policy-page__actions">
        <Link className="button button--secondary" to="/panel">Volver al panel</Link>
        <Link className="button button--primary" to={`/polizas/${polizaId}/cancelacion`} state={{ policy }}>Solicitar cancelación</Link>
      </div>
    </div>
  )
}

export default PolicyPage
