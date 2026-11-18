import { useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import PaymentForm from '../../components/payment/PaymentForm/PaymentForm.jsx'
import PaymentSummary from '../../components/payment/PaymentSummary/PaymentSummary.jsx'
import { quoteResultData } from '../../mocks/quoteResultData.js'
import { simulatePayment } from '../../mocks/paymentRules.js'
import './PaymentPage.css'

function PaymentPage() {
  const location = useLocation()
  const navigate = useNavigate()
  const [paymentError, setPaymentError] = useState('')
  const quoteType = location.state?.quoteType ?? 'AUTO'
  const quote = location.state?.quote ?? quoteResultData[quoteType] ?? quoteResultData.AUTO
  const backState = { quoteType, quote, values: location.state?.values }

  function handlePay(values) {
    const result = simulatePayment(values.cardNumber)
    if (result.status === 'REJECTED') {
      setPaymentError(result.message)
      return
    }

    const policyId = quoteType === 'MEDICAL' ? '802146' : '802145'
    navigate(`/polizas/${policyId}`, {
      state: { quoteType, quote, payment: { ...result, paymentMode: values.paymentMode, coverageStart: values.coverageStart } },
    })
  }

  return (
    <div className="payment-page page-container">
      <header className="payment-page__heading">
        <p className="eyebrow">Contratación</p>
        <h1>Confirma y realiza el pago</h1>
        <p>Los datos son demostrativos; el prototipo no procesa cargos reales.</p>
      </header>
      <aside className="payment-demo-guide" role="note">
        Usa <strong>4242 4242 4242 4242</strong> para aprobar o <strong>4000 0000 0000 0000</strong> para rechazar.
      </aside>
      <div className="payment-page__content">
        <PaymentSummary quote={quote} />
        <PaymentForm onPay={handlePay} paymentError={paymentError} backState={backState} />
      </div>
    </div>
  )
}

export default PaymentPage
