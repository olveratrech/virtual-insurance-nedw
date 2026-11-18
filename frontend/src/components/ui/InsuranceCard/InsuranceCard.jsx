import { Link } from 'react-router-dom'
import './InsuranceCard.css'

function InsuranceCard({ title, description, destination, actionLabel }) {
  return (
    <article className="insurance-card">
      <div>
        <p className="insurance-card__eyebrow">Seguro disponible</p>
        <h2>{title}</h2>
        <p>{description}</p>
      </div>
      <Link className="button button--primary" to={destination}>{actionLabel}</Link>
    </article>
  )
}

export default InsuranceCard
