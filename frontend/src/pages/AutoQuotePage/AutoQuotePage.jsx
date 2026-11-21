import { useLocation, useNavigate } from 'react-router-dom'
import AutoQuoteForm from '../../components/quote/AutoQuoteForm/AutoQuoteForm.jsx'
import QuotePageLayout from '../../components/quote/QuotePageLayout/QuotePageLayout.jsx'
import { createAutoQuote } from '../../services/quoteApi.js'

function AutoQuotePage() {
  const navigate = useNavigate()
  const location = useLocation()

  async function handleEvaluate(values) {
    const quote = await createAutoQuote(values)
    navigate(`/cotizaciones/${quote.id}`, { state: { quote } })
  }

  return (
    <QuotePageLayout eyebrow="Paso 1 de 2" title="Cotización de seguro de auto" description="Completa los datos del vehículo y del conductor.">
      <AutoQuoteForm onEvaluate={handleEvaluate} initialValues={location.state?.initialValues} />
    </QuotePageLayout>
  )
}

export default AutoQuotePage
