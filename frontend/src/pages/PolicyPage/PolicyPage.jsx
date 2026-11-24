import { useEffect, useState } from 'react'
import { Link, useLocation, useParams } from 'react-router-dom'
import DocumentList from '../../components/policy/DocumentList/DocumentList.jsx'
import PaymentOverview from '../../components/policy/PaymentOverview/PaymentOverview.jsx'
import PolicyDetails from '../../components/policy/PolicyDetails/PolicyDetails.jsx'
import CancellationHistory from '../../components/policy/CancellationHistory/CancellationHistory.jsx'
import StatusBadge from '../../components/ui/StatusBadge/StatusBadge.jsx'
import { downloadPolicyDocument, getPolicy } from '../../services/policyApi.js'
import './PolicyPage.css'

function PolicyPage() {
  const { key } = useLocation()
  return <PolicyPageContent key={key} />
}

function PolicyPageContent() {
  const { polizaId } = useParams()
  const location = useLocation()
  const [downloadMessage, setDownloadMessage] = useState('')
  const [downloadingId, setDownloadingId] = useState(null)
  const [policy, setPolicy] = useState(null)
  const [status, setStatus] = useState('loading')
  const [error, setError] = useState('')
  const [reloadKey, setReloadKey] = useState(0)
  const issuedAfterPayment = location.state?.issuedAfterPayment === true
  const active = policy?.status === 'ACTIVA'
  const cancellationRequested = policy?.status === 'CANCELACION_SOLICITADA'
  const cancelled = policy?.status === 'CANCELADA'
  const canRequestCancellation = active
  const statusPresentation = cancellationRequested
    ? {
        title: 'Cancelación en revisión',
        description: `La cobertura permanece vigente desde el ${policy.validFrom}.`,
        tone: 'warning',
        label: 'CANCELACIÓN SOLICITADA',
      }
    : cancelled
      ? {
          title: 'Tu póliza está cancelada',
          description: 'La cobertura ya no se encuentra activa.',
          tone: 'danger',
          label: 'CANCELADA',
        }
      : active
        ? {
            title: 'Tu póliza está activa',
            description: `Cobertura vigente desde el ${policy.validFrom}.`,
            tone: 'success',
            label: 'ACTIVA',
          }
        : {
            title: 'Estado de tu póliza',
            description: 'La cobertura no se encuentra activa.',
            tone: 'neutral',
            label: policy?.status,
          }

  useEffect(() => {
    let active = true
    getPolicy(polizaId)
      .then((response) => {
        if (!active) return
        setPolicy(response)
        setStatus('ready')
        setError('')
      })
      .catch((requestError) => {
        if (!active) return
        setError(requestError.message)
        setStatus('error')
      })
    return () => {
      active = false
    }
  }, [polizaId, reloadKey])

  function retry() {
    setStatus('loading')
    setError('')
    setReloadKey((value) => value + 1)
  }

  if (status === 'loading') {
    return <p className="policy-page page-container" role="status">Consultando póliza…</p>
  }

  if (status === 'error') {
    return (
      <section className="policy-page page-container" role="alert">
        <h1>No fue posible consultar la póliza</h1>
        <p>{error}</p>
        <div className="policy-page__actions">
          <Link className="button button--secondary" to="/panel">Volver al panel</Link>
          <button className="button button--primary" type="button" onClick={retry}>Reintentar</button>
        </div>
      </section>
    )
  }

  async function handleDownload(document) {
    setDownloadingId(document.id)
    setDownloadMessage('')
    try {
      const { blob, filename } = await downloadPolicyDocument(document.downloadUrl)
      const objectUrl = URL.createObjectURL(blob)
      const link = window.document.createElement('a')
      link.href = objectUrl
      link.download = filename
      window.document.body.appendChild(link)
      link.click()
      link.remove()
      window.setTimeout(() => URL.revokeObjectURL(objectUrl), 1000)
      setDownloadMessage(`${document.name} se descargó correctamente.`)
    } catch (requestError) {
      setDownloadMessage(requestError.message)
    } finally {
      setDownloadingId(null)
    }
  }

  return (
    <div className="policy-page page-container">
      <header className="policy-page__heading">
        <p className="eyebrow">Póliza {policy.policyNumber}</p>
        <div className="policy-page__title-row">
          <h1>{statusPresentation.title}</h1>
          <StatusBadge tone={statusPresentation.tone}>{statusPresentation.label}</StatusBadge>
        </div>
        <p>{statusPresentation.description}</p>
      </header>

      {issuedAfterPayment && <p className="confirmation-message" role="status">Pago aprobado. La póliza se emitió correctamente.</p>}
      {cancellationRequested && <p className="prototype-notice" role="status">La solicitud está en revisión. La cobertura permanece activa.</p>}
      {cancelled && <p className="policy-page__cancelled-notice" role="status">La solicitud fue aprobada. Esta póliza se conserva únicamente para consulta y descarga de documentos.</p>}

      <div className="policy-page__cards">
        <PolicyDetails policy={policy} />
        <PaymentOverview policy={policy} />
      </div>

      <CancellationHistory cancellation={policy.cancellation} />

      <DocumentList documents={policy.documents} onDownload={handleDownload} downloadingId={downloadingId} />
      {downloadMessage && <p className="prototype-notice" role="status">{downloadMessage}</p>}

      <div className="policy-page__actions">
        <Link className="button button--secondary" to="/panel">Volver al panel</Link>
        {canRequestCancellation && (
          <Link className="button button--primary" to={`/polizas/${polizaId}/cancelacion`}>Solicitar cancelación</Link>
        )}
      </div>
    </div>
  )
}

export default PolicyPage
