import './PolicyDetails.css'

function PolicyDetails({ policy }) {
  const details = [
    ['Producto', policy.productName],
    ['Cobertura', policy.coverageName],
    ['Inicio de vigencia', policy.validFrom],
    ['Fin de vigencia', policy.validUntil],
    [policy.insuredLabel, policy.insuredValue],
  ]

  return (
    <section className="policy-card" aria-labelledby="policy-details-title">
      <h2 id="policy-details-title">Datos de la póliza</h2>
      <dl className="policy-detail-list">
        {details.map(([label, value]) => (
          <div key={label}><dt>{label}</dt><dd>{value}</dd></div>
        ))}
      </dl>
    </section>
  )
}

export default PolicyDetails
