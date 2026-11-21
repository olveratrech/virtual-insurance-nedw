import StatusBadge from '../../ui/StatusBadge/StatusBadge.jsx'
import './CancellationHistory.css'

const statusPresentation = {
  SOLICITADA: { label: 'Cancelación solicitada', tone: 'warning' },
  APROBADA: { label: 'Cancelación aprobada', tone: 'success' },
  RECHAZADA: { label: 'Cancelación rechazada', tone: 'danger' },
}

const reasonLabels = {
  NOT_NEEDED: 'Ya no necesita la cobertura',
  FINANCIAL: 'Motivos económicos',
  SERVICE: 'Inconformidad con el servicio',
  OTHER: 'Otro motivo',
}

function CancellationHistory({ cancellation }) {
  if (!cancellation) return null
  const status = statusPresentation[cancellation.status] ?? {
    label: cancellation.status,
    tone: 'neutral',
  }

  return (
    <section className="cancellation-history" aria-labelledby="cancellation-history-title">
      <div className="cancellation-history__heading">
        <div>
          <p className="eyebrow">Historial de cancelación</p>
          <h2 id="cancellation-history-title">Solicitud {cancellation.requestNumber}</h2>
        </div>
        <StatusBadge tone={status.tone}>{status.label}</StatusBadge>
      </div>
      <dl>
        <div><dt>Motivo</dt><dd>{reasonLabels[cancellation.reason] ?? cancellation.reason}</dd></div>
        <div><dt>Fecha efectiva solicitada</dt><dd>{cancellation.requestedDateLabel}</dd></div>
        <div><dt>Registrada</dt><dd>{cancellation.createdAtLabel}</dd></div>
        {cancellation.resolvedAtLabel && <div><dt>Resuelta</dt><dd>{cancellation.resolvedAtLabel}</dd></div>}
        {cancellation.comments && <div className="cancellation-history__full"><dt>Comentarios</dt><dd>{cancellation.comments}</dd></div>}
        {cancellation.resolutionObservation && <div className="cancellation-history__full"><dt>Observación de resolución</dt><dd>{cancellation.resolutionObservation}</dd></div>}
      </dl>
    </section>
  )
}

export default CancellationHistory
