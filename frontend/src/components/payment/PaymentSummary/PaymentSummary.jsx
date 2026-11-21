import './PaymentSummary.css'
import { formatMxn, formatQuoteNumber } from '../../../utils/currency.js'

function PaymentSummary({ quote }) {
  return (
    <aside className="payment-summary" aria-labelledby="payment-summary-title">
      <h2 id="payment-summary-title">Resumen</h2>
      <dl>
        <div><dt>Producto</dt><dd>{quote.productName}</dd></div>
        <div><dt>Cobertura</dt><dd>{quote.coverageName}</dd></div>
        <div><dt>Cotización</dt><dd>{formatQuoteNumber(quote.quoteNumber)}</dd></div>
        <div className="payment-summary__total"><dt>Total anual</dt><dd>{formatMxn(quote.annualCost)}</dd></div>
      </dl>
    </aside>
  )
}

export default PaymentSummary
