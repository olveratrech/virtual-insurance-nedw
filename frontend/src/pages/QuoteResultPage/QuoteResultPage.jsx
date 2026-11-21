import { useEffect, useState } from 'react'
import { Link, useLocation, useParams } from 'react-router-dom'
import StatusBadge from '../../components/ui/StatusBadge/StatusBadge.jsx'
import { getQuote } from '../../services/quoteApi.js'
import { formatMxn, formatQuoteNumber } from '../../utils/currency.js'
import './QuoteResultPage.css'

function QuoteResultPage() {
  const { quoteId } = useParams()
  const location = useLocation()
  const [quote, setQuote] = useState(location.state?.quote ?? null)
  const [status, setStatus] = useState(quote ? 'ready' : 'loading')
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
      <header className="quote-result-page__heading">
        <p className="eyebrow">Resultado de cotización</p>
        <StatusBadge tone={approved ? 'success' : 'danger'}>{quote.status}</StatusBadge>
        <h1>{approved ? 'Tu cotización fue aprobada' : 'No podemos ofrecer esta cobertura'}</h1>
        <p>{approved ? 'Revisa la cobertura y el costo antes de continuar con la contratación.' : 'La evaluación encontró factores fuera de las reglas académicas de aceptación.'}</p>
      </header>

      <section className={`quote-result-card quote-result-card--${approved ? 'approved' : 'rejected'}`}>
        <div className="quote-result-card__summary">
          <div>
            <span className="quote-result-card__label">Cotización</span>
            <strong>{formatQuoteNumber(quote.quoteNumber)}</strong>
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
              <div><span>Prima anual</span><strong>{formatMxn(quote.annualCost)}</strong></div>
              <div><span>Pago mensual estimado</span><strong>{formatMxn(quote.monthlyCost)}</strong></div>
            </div>
            <div>
              <h2>Principales beneficios</h2>
              <ul className="benefit-list">
                {quote.benefits.map((benefit) => <li key={benefit}>{benefit}</li>)}
              </ul>
            </div>
            <p className="result-note">Vigencia de la cotización: {quote.validUntil}. La emisión queda sujeta al pago simulado.</p>
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
        {approved ? (
          <Link
            className="button button--primary"
            to="/contratacion/pago"
            state={{ quoteId: quote.id, quoteType: quote.type, quote }}
          >
            Contratar seguro
          </Link>
        ) : (
          <Link className="button button--primary" to={retryDestination} state={{ initialValues: quote.details }}>
            Revisar datos
          </Link>
        )}
      </div>
    </div>
  )
}

export default QuoteResultPage
