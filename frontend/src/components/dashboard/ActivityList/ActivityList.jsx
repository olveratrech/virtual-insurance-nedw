import { Link } from 'react-router-dom'
import StatusBadge from '../../ui/StatusBadge/StatusBadge.jsx'
import './ActivityList.css'

function ActivityList({ items }) {
  if (items.length === 0) return <p className="activity-list__empty">Todavía no hay actividad para mostrar.</p>

  return (
    <ul className="activity-list">
      {items.map((item) => (
        <li key={item.id}>
          <Link to={item.destination}>
            <div className="activity-list__main">
              <div className="activity-list__title">
                <strong>{item.type}</strong>
                <span>{item.reference}</span>
                <StatusBadge tone={item.statusTone}>{item.status}</StatusBadge>
              </div>
              <p>{item.detail}</p>
            </div>
            <span className="activity-list__arrow" aria-hidden="true">→</span>
          </Link>
        </li>
      ))}
    </ul>
  )
}

export default ActivityList
