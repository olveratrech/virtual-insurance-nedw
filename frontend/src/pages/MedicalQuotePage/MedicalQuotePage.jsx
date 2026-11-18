import { useLocation, useNavigate } from 'react-router-dom'
import MedicalQuoteForm from '../../components/quote/MedicalQuoteForm/MedicalQuoteForm.jsx'
import QuotePageLayout from '../../components/quote/QuotePageLayout/QuotePageLayout.jsx'
import { evaluateMedicalQuote } from '../../mocks/quoteRules.js'

function MedicalQuotePage() {
  const navigate = useNavigate()
  const location = useLocation()

  function handleEvaluate(values) {
    const decision = evaluateMedicalQuote(values)
    navigate(`/cotizaciones/${decision === 'approved' ? 'aprobada' : 'rechazada'}`, { state: { quoteType: 'MEDICAL', values } })
  }

  return (
    <QuotePageLayout eyebrow="Paso 1 de 2" title="Cotización de seguro médico" description="Indica la edad y los factores de salud declarados del asegurado.">
      <MedicalQuoteForm onEvaluate={handleEvaluate} initialValues={location.state?.initialValues} />
    </QuotePageLayout>
  )
}

export default MedicalQuotePage
