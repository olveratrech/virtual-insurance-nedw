import { useEffect, useState } from 'react'
import { Link, useLocation, useParams } from 'react-router-dom'
import StatusBadge from '../../components/ui/StatusBadge/StatusBadge.jsx'
import FlowSteps from '../../components/quote/FlowSteps/FlowSteps.jsx'
import { getQuote } from '../../services/quoteApi.js'
import { formatMxn, formatQuoteNumber } from '../../utils/currency.js'
import './QuoteResultPage.css'

function QuoteResultPage() {
  const { key } = useLocation()
  return <QuoteResultPageContent key={key} />
}

function QuoteResultPageContent() {
  const { quoteId } = useParams()
  const [quote, setQuote] = useState(null)
  const [status, setStatus] = useState('loading')
  const [error, setError] = useState('')
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    let active = true

    getQuote(quoteId)
      .then((response) => {
        if (!active) return
        setQuote(response)
        setStatus('ready')
      })
      .catch((requestError) => {
        if (!active) return
        setError(requestError.message)
        setStatus('error')
      })

    return () => {
      active = false
    }
  }, [quoteId, reloadKey])

  function retry() {
    setStatus('loading')
    setError('')
    setReloadKey((value) => value + 1)
  }

  if (status === 'loading') {
    return <p className="quote-result-page page-container" role="status">Consultando cotización…</p>
  }

  if (status === 'error') {
    return (
      <section className="quote-result-page page-container" role="alert">
        <h1>No fue posible consultar la cotización</h1>
        <p>{error}</p>
        <div className="quote-result-page__actions">
          <Link className="button button--secondary" to="/panel">Volver al panel</Link>
          <button className="button button--primary" type="button" onClick={retry}>
            Reintentar
          </button>
        </div>
      </section>
    )
  }

  const approved = quote.status === 'APROBADA'
  const retryDestination = quote.type === 'MEDICAL' ? '/cotizar/medico' : '/cotizar/auto'

  return (
    <div className="quote-result-page page-container">
      <FlowSteps current={2} />
      <header className="quote-result-page__heading">
        <p className="eyebrow">Resultado de cotización</p>
        <StatusBadge tone={approved ? 'success' : 'danger'}>{quote.status}</StatusBadge>
        <h1>{approved ? 'Tu cotización fue aprobada' : 'No podemos ofrecer esta cobertura'}</h1>
        <p>{approved ? (quote.contracted ? 'Esta cotización ya fue contratada y conserva su póliza vinculada.' : 'Revisa la cobertura y el costo antes de continuar con la contratación.') : 'La evaluación encontró factores fuera de las reglas de aceptación.'}</p>
      </header>

      <section className={`quote-result-card quote-result-card--${approved ? 'approved' : 'rejected'}`}>
        <div className="quote-result-card__summary">
          <div>
            <span className="quote-result-card__label">Cotización</span>
            <strong className="technical-data">{formatQuoteNumber(quote.quoteNumber)}</strong>
          </div>
          <div>
            <span className="quote-result-card__label">Producto</span>
            <strong>{quote.productName}</strong>
          </div>
          <div>
            <span className="quote-result-card__label">Cobertura</span>
            <strong>{quote.coverageName}</strong>
          </div>
        </div>

        {approved ? (
          <>
            <div className="quote-result-card__prices">
              <div><span>Prima anual</span><strong className="technical-data">{formatMxn(quote.annualCost)}</strong></div>
              <div><span>Pago mensual estimado</span><strong className="technical-data">{formatMxn(quote.monthlyCost)}</strong></div>
            </div>
            <div>
              <h2>Principales beneficios</h2>
              <ul className="benefit-list">
                {quote.benefits.map((benefit) => <li key={benefit}>{benefit}</li>)}
              </ul>
            </div>
            <p className="result-note">Vigencia de la cotización: {quote.validUntil}. La emisión queda sujeta a la confirmación del pago.</p>
          </>
        ) : (
          <div className="rejection-explanation">
            <h2>Resultado informativo</h2>
            <p>{quote.rejectionReason}</p>
            <p>No se generó póliza ni se realizó cargo alguno. La aplicación no muestra diagnósticos sensibles.</p>
          </div>
        )}
      </section>

      <div className="quote-result-page__actions">
        <Link className="button button--secondary" to="/panel">Volver al panel</Link>
        {approved && quote.contracted ? (
          <Link className="button button--primary" to={`/polizas/${quote.policyId}`}>
            Ver póliza
          </Link>
        ) : approved ? (
          <Link
            className="button button--primary"
            to={`/cotizaciones/${quote.id}/pago`}
          >
            Contratar seguro
          </Link>
        ) : (
          <Link className="button button--primary" to={`${retryDestination}?revisar=${quote.id}`}>
            Revisar datos
          </Link>
        )}
      </div>
    </div>
  )
}

export default QuoteResultPage
