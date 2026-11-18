import { Link, useLocation } from 'react-router-dom'
import StatusBadge from '../../components/ui/StatusBadge/StatusBadge.jsx'
import { quoteResultData } from '../../mocks/quoteResultData.js'
import './QuoteResultPage.css'

function QuoteResultPage({ outcome }) {
  const location = useLocation()
  const fallbackType = outcome === 'approved' ? 'AUTO' : 'MEDICAL'
  const quoteType = location.state?.quoteType ?? fallbackType
  const result = quoteResultData[quoteType] ?? quoteResultData[fallbackType]
  const approved = outcome === 'approved'

  return (
    <div className="quote-result-page page-container">
      <header className="quote-result-page__heading">
        <p className="eyebrow">Resultado de cotización</p>
        <StatusBadge tone={approved ? 'success' : 'danger'}>
          {approved ? 'APROBADA' : 'RECHAZADA'}
        </StatusBadge>
        <h1>{approved ? 'Tu cotización fue aprobada' : 'No podemos ofrecer esta cobertura'}</h1>
        <p>{approved ? 'Revisa la cobertura y el costo antes de continuar con la contratación.' : 'La evaluación encontró factores fuera de las reglas provisionales de aceptación.'}</p>
      </header>

      <section className={`quote-result-card quote-result-card--${approved ? 'approved' : 'rejected'}`}>
        <div className="quote-result-card__summary">
          <div>
            <span className="quote-result-card__label">Cotización</span>
            <strong>{result.quoteNumber}</strong>
          </div>
          <div>
            <span className="quote-result-card__label">Producto</span>
            <strong>{result.productName}</strong>
          </div>
          <div>
            <span className="quote-result-card__label">Cobertura</span>
            <strong>{result.coverageName}</strong>
          </div>
        </div>

        {approved ? (
          <>
            <div className="quote-result-card__prices">
              <div><span>Prima anual</span><strong>{result.annualCost}</strong></div>
              <div><span>Pago mensual estimado</span><strong>{result.monthlyCost}</strong></div>
            </div>
            <div>
              <h2>Principales beneficios</h2>
              <ul className="benefit-list">
                {result.benefits.map((benefit) => <li key={benefit}>{benefit}</li>)}
              </ul>
            </div>
            <p className="result-note">La emisión de la póliza queda sujeta a la confirmación y al pago simulado.</p>
          </>
        ) : (
          <div className="rejection-explanation">
            <h2>Resultado informativo</h2>
            <p>{result.rejectionReason}</p>
            <p>No se generó póliza ni se realizó cargo alguno. El prototipo no muestra diagnósticos sensibles.</p>
          </div>
        )}
      </section>

      <div className="quote-result-page__actions">
        <Link className="button button--secondary" to="/panel">Volver al panel</Link>
        {approved ? (
          <Link className="button button--primary" to="/contratacion/pago" state={{ quoteType, quote: result }}>Contratar seguro</Link>
        ) : (
          <Link className="button button--primary" to={result.retryDestination} state={{ initialValues: location.state?.values }}>Revisar datos</Link>
        )}
      </div>
    </div>
  )
}

export default QuoteResultPage
