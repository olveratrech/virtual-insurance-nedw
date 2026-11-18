import { useLocation } from 'react-router-dom'
import ActivityList from '../../components/dashboard/ActivityList/ActivityList.jsx'
import DashboardActionCard from '../../components/dashboard/DashboardActionCard/DashboardActionCard.jsx'
import { dashboardData } from '../../mocks/dashboardData.js'
import './DashboardPage.css'

function DashboardPage() {
  const location = useLocation()
  const confirmationMessage = location.state?.message

  return (
    <div className="dashboard-page page-container">
      <header className="dashboard-page__heading">
        <p className="eyebrow">Mi cuenta</p>
        <h1>Hola, {dashboardData.customerName}</h1>
        <p>Consulta tus operaciones o inicia una nueva cotización.</p>
      </header>

      {confirmationMessage && <p className="confirmation-message" role="status">{confirmationMessage}</p>}

      <section aria-labelledby="dashboard-actions-title">
        <div className="section-heading"><h2 id="dashboard-actions-title">¿Qué quieres hacer?</h2></div>
        <div className="dashboard-page__actions">
          {dashboardData.actions.map((action) => <DashboardActionCard key={action.destination} {...action} />)}
        </div>
      </section>

      <section aria-labelledby="recent-activity-title">
        <div className="section-heading"><h2 id="recent-activity-title">Actividad reciente</h2></div>
        <ActivityList items={dashboardData.recentActivity} />
      </section>
    </div>
  )
}

export default DashboardPage
