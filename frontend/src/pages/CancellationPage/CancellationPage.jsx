import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import CancellationForm from '../../components/cancellation/CancellationForm/CancellationForm.jsx'
import StatusBadge from '../../components/ui/StatusBadge/StatusBadge.jsx'
import { ApiError } from '../../services/apiClient.js'
import { createCancellationRequest } from '../../services/cancellationApi.js'
import { getPolicy } from '../../services/policyApi.js'
import NotFoundPage from '../NotFoundPage/NotFoundPage.jsx'
import './CancellationPage.css'

function CancellationPage() {
  const { polizaId } = useParams()
  const [policy, setPolicy] = useState(null)
  const [request, setRequest] = useState(null)
  const [status, setStatus] = useState('loading')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState('')
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    let active = true
    getPolicy(polizaId)
      .then((response) => {
        if (!active) return
        setPolicy(response)
        if (response.status === 'CANCELACION_SOLICITADA') {
          setError('')
          setStatus('pending')
        } else if (response.status !== 'ACTIVA') {
          setError('Solo una póliza activa puede recibir una solicitud de cancelación.')
          setStatus('not-active')
        } else {
          setError('')
          setStatus('ready')
        }
      })
      .catch((requestError) => {
        if (!active) return
        setError(requestError.message)
        setStatus(requestError instanceof ApiError && requestError.status === 404 ? 'not-found' : 'error')
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

  async function handleSubmitRequest(values) {
    if (submitting) return
    setSubmitting(true)
    setSubmitError('')
    try {
      const response = await createCancellationRequest(polizaId, values)
      setRequest(response)
      setPolicy((current) => ({ ...current, status: response.policyStatus }))
      setStatus('confirmed')
    } catch (requestError) {
      const code = requestError instanceof ApiError ? requestError.problem?.code : null
      if (code === 'CANCELLATION_ALREADY_PENDING') {
        setStatus('pending')
      } else if (code === 'POLICY_NOT_ACTIVE') {
        setError(requestError.message)
        setStatus('not-active')
      } else if (requestError instanceof ApiError && requestError.status === 404) {
        setStatus('not-found')
      } else {
        setSubmitError(requestError.message)
      }
    } finally {
      setSubmitting(false)
    }
  }

  if (status === 'loading') {
    return <p className="cancellation-page page-container" role="status">Consultando póliza…</p>
  }

  if (status === 'not-found') {
    return <NotFoundPage title="Póliza no encontrada" message="No se puede solicitar la cancelación porque la póliza indicada no existe." backTo="/panel" backLabel="Volver al panel" />
  }

  if (status === 'error') {
    return (
      <section className="cancellation-page page-container" role="alert">
        <h1>No fue posible consultar la póliza</h1>
        <p>{error}</p>
        <div className="cancellation-confirmation__actions">
          <Link className="button button--secondary" to="/panel">Volver al panel</Link>
          <button className="button button--primary" type="button" onClick={retry}>Reintentar</button>
        </div>
      </section>
    )
  }

  return (
    <div className="cancellation-page page-container">
      <header className="cancellation-page__heading">
        <p className="eyebrow">Gestión de póliza</p>
        <h1>Solicitar cancelación</h1>
        <p>{policy.policyNumber} · Estado actual: {policy.statusLabel}</p>
      </header>

      {status === 'confirmed' && request ? (
        <section className="cancellation-confirmation" aria-labelledby="cancellation-confirmation-title">
          <StatusBadge tone="warning">CANCELACIÓN SOLICITADA</StatusBadge>
          <h2 id="cancellation-confirmation-title">Recibimos tu solicitud</h2>
          <p>Folio: <strong>{request.requestNumber}</strong></p>
          <p>La póliza permanece activa mientras se revisa la solicitud. No se ha efectuado devolución ni cambio de cobertura.</p>
          <div className="cancellation-confirmation__actions">
            <Link className="button button--primary" to={`/polizas/${polizaId}`}>Volver a la póliza</Link>
            <Link className="button button--secondary" to="/panel">Ir al panel</Link>
          </div>
        </section>
      ) : status === 'pending' ? (
        <section className="cancellation-confirmation" aria-labelledby="cancellation-pending-title">
          <StatusBadge tone="warning">CANCELACIÓN SOLICITADA</StatusBadge>
          <h2 id="cancellation-pending-title">Esta póliza ya tiene una solicitud en revisión</h2>
          {policy.cancellation?.requestNumber && <p>Folio: <strong>{policy.cancellation.requestNumber}</strong></p>}
          <p>La cobertura permanece activa mientras se resuelve la solicitud. No es necesario enviarla nuevamente.</p>
          <div className="cancellation-confirmation__actions">
            <Link className="button button--primary" to={`/polizas/${polizaId}`}>Volver a la póliza</Link>
            <Link className="button button--secondary" to="/panel">Ir al panel</Link>
          </div>
        </section>
      ) : status === 'not-active' ? (
        <section className="cancellation-confirmation" aria-labelledby="cancellation-unavailable-title">
          <StatusBadge tone="danger">NO DISPONIBLE</StatusBadge>
          <h2 id="cancellation-unavailable-title">No se puede solicitar la cancelación</h2>
          <p>{error}</p>
          <div className="cancellation-confirmation__actions">
            <Link className="button button--primary" to={`/polizas/${polizaId}`}>Volver a la póliza</Link>
          </div>
        </section>
      ) : (
        <CancellationForm
          policyId={polizaId}
          onSubmitRequest={handleSubmitRequest}
          submitting={submitting}
          submitError={submitError}
        />
      )}
    </div>
  )
}

export default CancellationPage
