import { useNavigate, useSearchParams } from 'react-router-dom'
import AutoQuoteForm from '../../components/quote/AutoQuoteForm/AutoQuoteForm.jsx'
import QuotePageLayout from '../../components/quote/QuotePageLayout/QuotePageLayout.jsx'
import { useQuoteDraft } from '../../hooks/useQuoteDraft.js'
import { createAutoQuote } from '../../services/quoteApi.js'

function AutoQuotePage() {
  const navigate = useNavigate()
  const [params] = useSearchParams()
  const quoteId = params.get('revisar')
  const draft = useQuoteDraft(quoteId, 'AUTO')

  async function handleEvaluate(values) {
    const quote = await createAutoQuote(values)
    navigate(`/cotizaciones/${quote.id}`)
  }

  return (
    <QuotePageLayout eyebrow="Paso 1 de 2" title="Cotización de seguro de auto" description="Completa los datos del vehículo y del conductor.">
      {draft.loading && <p role="status">Consultando cotización…</p>}
      {draft.error && <p role="alert">No fue posible recuperar los datos de la cotización.</p>}
      {!draft.loading && <AutoQuoteForm key={quoteId} onEvaluate={handleEvaluate} initialValues={draft.values} />}
    </QuotePageLayout>
  )
}

export default AutoQuotePage
