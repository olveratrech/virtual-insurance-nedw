import { useNavigate, useSearchParams } from 'react-router-dom'
import MedicalQuoteForm from '../../components/quote/MedicalQuoteForm/MedicalQuoteForm.jsx'
import QuotePageLayout from '../../components/quote/QuotePageLayout/QuotePageLayout.jsx'
import { useQuoteDraft } from '../../hooks/useQuoteDraft.js'
import { createMedicalQuote } from '../../services/quoteApi.js'

function MedicalQuotePage() {
  const navigate = useNavigate()
  const [params] = useSearchParams()
  const quoteId = params.get('revisar')
  const draft = useQuoteDraft(quoteId, 'MEDICAL')

  async function handleEvaluate(values) {
    const quote = await createMedicalQuote(values)
    navigate(`/cotizaciones/${quote.id}`)
  }

  return (
    <QuotePageLayout eyebrow="Paso 1 de 2" title="Cotización de seguro médico" description="Indica la edad y los factores de salud declarados del asegurado.">
      {draft.loading && <p role="status">Consultando cotización…</p>}
      {draft.error && <p role="alert">No fue posible recuperar los datos de la cotización.</p>}
      {!draft.loading && <MedicalQuoteForm key={quoteId} onEvaluate={handleEvaluate} initialValues={draft.values} />}
    </QuotePageLayout>
  )
}

export default MedicalQuotePage
