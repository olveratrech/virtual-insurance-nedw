import { Link } from 'react-router-dom'
import './DashboardActionCard.css'

function DashboardActionCard({ title, description, destination, actionLabel }) {
  return (
    <article className="dashboard-action-card">
      <div>
        <h3>{title}</h3>
        <p>{description}</p>
      </div>
      <Link className="button button--primary" to={destination}>{actionLabel}</Link>
    </article>
  )
}

export default DashboardActionCard
