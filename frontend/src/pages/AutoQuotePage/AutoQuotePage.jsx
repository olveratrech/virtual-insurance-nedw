import { useLocation, useNavigate } from 'react-router-dom'
import AutoQuoteForm from '../../components/quote/AutoQuoteForm/AutoQuoteForm.jsx'
import QuotePageLayout from '../../components/quote/QuotePageLayout/QuotePageLayout.jsx'
import { evaluateAutoQuote } from '../../mocks/quoteRules.js'

function AutoQuotePage() {
  const navigate = useNavigate()
  const location = useLocation()

  function handleEvaluate(values) {
    const decision = evaluateAutoQuote(values)
    navigate(`/cotizaciones/${decision === 'approved' ? 'aprobada' : 'rechazada'}`, { state: { quoteType: 'AUTO', values } })
  }

  return (
    <QuotePageLayout eyebrow="Paso 1 de 2" title="Cotización de seguro de auto" description="Completa los datos del vehículo y del conductor.">
      <AutoQuoteForm onEvaluate={handleEvaluate} initialValues={location.state?.initialValues} />
    </QuotePageLayout>
  )
}

export default AutoQuotePage
