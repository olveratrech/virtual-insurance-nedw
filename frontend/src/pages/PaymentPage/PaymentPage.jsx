import { useEffect, useRef, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import PaymentForm from '../../components/payment/PaymentForm/PaymentForm.jsx'
import PaymentSummary from '../../components/payment/PaymentSummary/PaymentSummary.jsx'
import FlowSteps from '../../components/quote/FlowSteps/FlowSteps.jsx'
import { createPaymentAttempt } from '../../services/paymentApi.js'
import { getQuote } from '../../services/quoteApi.js'
import { formatMxn } from '../../utils/currency.js'
import './PaymentPage.css'

function PaymentPage() {
  const { quoteId } = useParams()
  const navigate = useNavigate()
  const [quote, setQuote] = useState(null)
  const [loadStatus, setLoadStatus] = useState('loading')
  const [loadError, setLoadError] = useState('')
  const [paymentError, setPaymentError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const idempotencyKey = useRef(crypto.randomUUID())
  const backDestination = `/cotizaciones/${quoteId}`

  useEffect(() => {
    let active = true
    setLoadStatus('loading')
    setLoadError('')

    getQuote(quoteId)
      .then((response) => {
        if (!active) return
        setQuote(response)
        setLoadStatus('ready')
      })
      .catch((requestError) => {
        if (!active) return
        setLoadError(requestError.message)
        setLoadStatus('error')
      })

    return () => {
      active = false
    }
  }, [quoteId])

  async function handlePay(values) {
    setPaymentError('')
    setIsSubmitting(true)
    try {
      const result = await createPaymentAttempt(quoteId, {
        coverageStart: values.coverageStart,
        paymentMode: values.paymentMode,
        cardholderName: values.cardholderName.trim(),
        cardBrand: values.cardBrand,
        cardNumber: values.cardNumber.replace(/\s/g, ''),
        expiration: values.expiration,
        cvv: values.cvv,
        acceptsTerms: values.acceptsTerms,
      }, idempotencyKey.current)

      if (result.status === 'RECHAZADO') {
        setPaymentError(`${result.message} No se emitió ninguna póliza.`)
        idempotencyKey.current = crypto.randomUUID()
        return
      }

      const policy = {
        policyNumber: result.policyNumber,
        status: 'ACTIVA',
        type: quote.type,
        productName: quote.productName,
        coverageName: quote.coverageName,
        validFrom: new Date(`${result.coverageStart}T12:00:00`).toLocaleDateString('es-MX', { dateStyle: 'long' }),
        validUntil: new Date(`${result.coverageEnd}T12:00:00`).toLocaleDateString('es-MX', { dateStyle: 'long' }),
        insuredLabel: quote.type === 'MEDICAL' ? 'Persona asegurada' : 'Vehículo asegurado',
        insuredValue: quote.type === 'MEDICAL'
          ? 'Cliente autenticado'
          : `${quote.details.brand} ${quote.details.model} · ${quote.details.year}`,
        paymentMode: values.paymentMode === 'ANNUAL' ? 'Anual' : 'Mensual',
        nextPayment: values.paymentMode === 'ANNUAL' ? 'Cubierto durante la vigencia' : 'Un mes después del primer pago',
        lastPayment: `${formatMxn(result.amount)} · APLICADO`,
        documents: [
          { id: 'policy-pdf', name: 'Póliza de seguro', format: 'PDF' },
          { id: 'payment-receipt', name: 'Comprobante de pago', format: 'PDF' },
        ],
      }
      navigate(`/polizas/${result.policyId}`, {
        state: { quoteType: quote.type, quote, policy, payment: result },
      })
    } catch (requestError) {
      setPaymentError(requestError.message)
    } finally {
      setIsSubmitting(false)
    }
  }

  if (loadStatus === 'loading') {
    return <p className="payment-page page-container route-status" role="status">Verificando disponibilidad de la cotización…</p>
  }

  if (loadStatus === 'error') {
    return (
      <section className="payment-page page-container route-status" role="alert">
        <h1>No fue posible preparar la contratación</h1>
        <p>{loadError}</p>
        <Link className="button button--secondary" to="/panel">Volver al panel</Link>
      </section>
    )
  }

  if (quote.contracted) {
    return (
      <section className="payment-page page-container route-status">
        <p className="eyebrow">Contratación completada</p>
        <h1>Esta cotización ya tiene una póliza</h1>
        <p>Una cotización contratada no puede generar otro pago, aunque su póliza haya cambiado de estado.</p>
        <div className="payment-form__actions">
          <Link className="button button--secondary" to={backDestination}>Volver a la cotización</Link>
          <Link className="button button--primary" to={`/polizas/${quote.policyId}`}>Ver póliza</Link>
        </div>
      </section>
    )
  }

  if (quote.status !== 'APROBADA') {
    return (
      <section className="payment-page page-container route-status">
        <h1>Esta cotización no está disponible para contratación</h1>
        <p>Solo una cotización aprobada puede continuar al pago.</p>
        <Link className="button button--secondary" to={backDestination}>Volver a la cotización</Link>
      </section>
    )
  }

  return (
    <div className="payment-page page-container">
      <FlowSteps current={3} />
      <header className="payment-page__heading">
        <p className="eyebrow">Contratación</p>
        <h1>Confirma y realiza el pago</h1>
        <p>Revisa la cobertura, la modalidad y los datos de pago antes de confirmar.</p>
      </header>
      <aside className="payment-demo-guide" role="note">
        Datos habilitados para evaluación: usa vencimiento <strong>12/30</strong> y CVV <strong>123</strong> para Visa/Mastercard o <strong>1234</strong> para American Express. Con fondos: Visa <strong>4242 4242 4242 1111</strong>, Mastercard <strong>5105 1051 0510 5100</strong> y American Express <strong>3714 496353 98431</strong>. Casos Visa: <strong>4000 0000 0000 2222</strong> sin fondos, <strong>4000 0000 0000 3333</strong> bloqueada o
        {' '}<strong>4000 0000 0000 5555</strong> rechazada por el procesador.
      </aside>
      <div className="payment-page__content">
        <PaymentSummary quote={quote} />
        <PaymentForm
          onPay={handlePay}
          paymentError={paymentError}
          backDestination={backDestination}
          isSubmitting={isSubmitting}
        />
      </div>
    </div>
  )
}

export default PaymentPage
