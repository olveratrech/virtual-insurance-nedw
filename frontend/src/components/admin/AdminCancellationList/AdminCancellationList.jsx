import { Link } from 'react-router-dom'
import StatusBadge from '../../ui/StatusBadge/StatusBadge.jsx'
import {
  formatAdminDate,
  presentCancellationReason,
  presentCancellationStatus,
} from '../../../utils/adminCancellation.js'
import './AdminCancellationList.css'

function AdminCancellationList({ items }) {
  if (!items.length) {
    return <p className="admin-cancellation-list__empty">No hay solicitudes con el estado seleccionado.</p>
  }

  return (
    <div className="admin-cancellation-list">
      {items.map((item) => {
        const status = presentCancellationStatus(item.status)
        return (
          <article className="admin-cancellation-card" key={item.id}>
            <div className="admin-cancellation-card__heading">
              <div>
                <p className="admin-cancellation-card__folio">{item.requestNumber}</p>
                <h2>{item.client.fullName}</h2>
              </div>
              <StatusBadge tone={status.tone}>{status.label}</StatusBadge>
            </div>
            <dl>
              <div><dt>Póliza</dt><dd>{item.policy.policyNumber}</dd></div>
              <div><dt>Producto</dt><dd>{item.policy.productName}</dd></div>
              <div><dt>Motivo</dt><dd>{presentCancellationReason(item.reason)}</dd></div>
              <div><dt>Fecha efectiva</dt><dd>{formatAdminDate(item.requestedDate)}</dd></div>
            </dl>
            <Link className="button button--secondary" to={`/admin/cancelaciones/${item.id}`}>
              Revisar solicitud
            </Link>
          </article>
        )
      })}
    </div>
  )
}

export default AdminCancellationList
