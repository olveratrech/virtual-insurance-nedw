function PaymentOverview({ policy }) {
  return (
    <section className="policy-card" aria-labelledby="payment-overview-title">
      <h2 id="payment-overview-title">Pagos</h2>
      <dl className="policy-detail-list">
        <div><dt>Modalidad</dt><dd>{policy.paymentMode}</dd></div>
        <div><dt>Próximo pago</dt><dd>{policy.nextPayment}</dd></div>
        <div><dt>Último pago</dt><dd>{policy.lastPayment}</dd></div>
      </dl>
    </section>
  )
}

export default PaymentOverview
