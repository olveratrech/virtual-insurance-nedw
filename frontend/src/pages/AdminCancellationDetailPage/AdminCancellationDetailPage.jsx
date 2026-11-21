import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import AdminResolutionForm from '../../components/admin/AdminResolutionForm/AdminResolutionForm.jsx'
import StatusBadge from '../../components/ui/StatusBadge/StatusBadge.jsx'
import {
  formatAdminDate,
  formatAdminDateTime,
  presentCancellationReason,
  presentCancellationStatus,
} from '../../utils/adminCancellation.js'
import { getAdminCancellation, resolveAdminCancellation } from '../../services/adminCancellationApi.js'
import './AdminCancellationDetailPage.css'

function AdminCancellationDetailPage() {
  const { cancellationId } = useParams()
  const [cancellation, setCancellation] = useState(null)
  const [status, setStatus] = useState('loading')
  const [error, setError] = useState('')
  const [confirmation, setConfirmation] = useState('')
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    let active = true
    getAdminCancellation(cancellationId)
      .then((response) => {
        if (!active) return
        setCancellation(response)
        setError('')
        setStatus('ready')
      })
      .catch((requestError) => {
        if (!active) return
        setError(requestError.message)
        setStatus('error')
      })
    return () => { active = false }
  }, [cancellationId, reloadKey])

  async function handleResolve(resolution) {
    const response = await resolveAdminCancellation(cancellationId, resolution)
    setCancellation(response)
    setConfirmation(response.status === 'APROBADA'
      ? 'La cancelación fue aprobada y la póliza quedó cancelada.'
      : 'La cancelación fue rechazada y la póliza regresó a activa.')
  }

  if (status === 'loading') return <p className="page-container route-status" role="status">Consultando solicitud…</p>

  if (status === 'error') {
    return (
      <section className="admin-cancellation-detail page-container" role="alert">
        <h1>No fue posible consultar la solicitud</h1>
        <p>{error}</p>
        <div className="admin-cancellation-detail__actions">
          <Link className="button button--secondary" to="/admin/cancelaciones">Volver al listado</Link>
          <button className="button button--primary" type="button" onClick={() => { setStatus('loading'); setReloadKey((value) => value + 1) }}>Reintentar</button>
        </div>
      </section>
    )
  }

  const presentedStatus = presentCancellationStatus(cancellation.status)

  return (
    <div className="admin-cancellation-detail page-container">
      <header>
        <p className="eyebrow">Solicitud {cancellation.requestNumber}</p>
        <div className="admin-cancellation-detail__title">
          <h1>Revisión de cancelación</h1>
          <StatusBadge tone={presentedStatus.tone}>{presentedStatus.label}</StatusBadge>
        </div>
        <p>Creada el {formatAdminDateTime(cancellation.createdAt)}.</p>
      </header>

      {confirmation && <p className="confirmation-message" role="status">{confirmation}</p>}

      <div className="admin-cancellation-detail__grid">
        <section className="admin-detail-card" aria-labelledby="request-detail-title">
          <h2 id="request-detail-title">Solicitud</h2>
          <dl>
            <div><dt>Motivo</dt><dd>{presentCancellationReason(cancellation.reason)}</dd></div>
            <div><dt>Fecha efectiva</dt><dd>{formatAdminDate(cancellation.requestedDate)}</dd></div>
            <div className="admin-detail-card__full"><dt>Comentarios del cliente</dt><dd>{cancellation.comments || 'Sin comentarios adicionales.'}</dd></div>
          </dl>
        </section>

        <section className="admin-detail-card" aria-labelledby="policy-detail-title">
          <h2 id="policy-detail-title">Póliza y cliente</h2>
          <dl>
            <div><dt>Póliza</dt><dd>{cancellation.policy.policyNumber}</dd></div>
            <div><dt>Estado de póliza</dt><dd>{cancellation.policy.status}</dd></div>
            <div><dt>Producto</dt><dd>{cancellation.policy.productName}</dd></div>
            <div><dt>Cliente</dt><dd>{cancellation.client.fullName}</dd></div>
            <div className="admin-detail-card__full"><dt>Correo</dt><dd>{cancellation.client.email}</dd></div>
          </dl>
        </section>
      </div>

      {cancellation.status === 'SOLICITADA' ? (
        <AdminResolutionForm onResolve={handleResolve} />
      ) : (
        <section className="admin-detail-card admin-detail-card--resolution" aria-labelledby="resolution-title">
          <h2 id="resolution-title">Resolución registrada</h2>
          <dl>
            <div><dt>Responsable</dt><dd>{cancellation.resolution.resolvedByName}</dd></div>
            <div><dt>Fecha</dt><dd>{formatAdminDateTime(cancellation.resolution.resolvedAt)}</dd></div>
            <div className="admin-detail-card__full"><dt>Observación</dt><dd>{cancellation.resolution.observation}</dd></div>
          </dl>
        </section>
      )}

      <div className="admin-cancellation-detail__actions">
        <Link className="button button--secondary" to="/admin/cancelaciones">Volver al listado</Link>
      </div>
    </div>
  )
}

export default AdminCancellationDetailPage
