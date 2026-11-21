import { useEffect, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import ActivityList from '../../components/dashboard/ActivityList/ActivityList.jsx'
import DashboardActionCard from '../../components/dashboard/DashboardActionCard/DashboardActionCard.jsx'
import useAuth from '../../hooks/useAuth.js'
import { getPanel } from '../../services/panelApi.js'
import './DashboardPage.css'

const dashboardActions = [
  {
    title: 'Cotizar seguro de auto',
    description: 'Calcula una cobertura de acuerdo con tu vehículo y antecedentes.',
    destination: '/cotizar/auto',
    actionLabel: 'Nueva cotización de auto',
  },
  {
    title: 'Cotizar seguro médico',
    description: 'Conoce una propuesta basada en los datos de salud declarados.',
    destination: '/cotizar/medico',
    actionLabel: 'Nueva cotización médica',
  },
]

function DashboardPage() {
  const location = useLocation()
  const { client } = useAuth()
  const confirmationMessage = location.state?.message
  const [panel, setPanel] = useState(null)
  const [status, setStatus] = useState('loading')
  const [error, setError] = useState('')
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    let active = true
    getPanel()
      .then((response) => {
        if (!active) return
        setPanel(response)
        setError('')
        setStatus('ready')
      })
      .catch((requestError) => {
        if (!active) return
        setError(requestError.message)
        setStatus('error')
      })
    return () => {
      active = false
    }
  }, [reloadKey])

  function retry() {
    setStatus('loading')
    setError('')
    setReloadKey((value) => value + 1)
  }

  return (
    <div className="dashboard-page page-container">
      <header className="dashboard-page__heading">
        <p className="eyebrow">Mi cuenta</p>
        <h1>Hola, {panel?.clientName ?? client.name}</h1>
        <p>Consulta tus operaciones o inicia una nueva cotización.</p>
      </header>

      {confirmationMessage && <p className="confirmation-message" role="status">{confirmationMessage}</p>}

      <section aria-labelledby="dashboard-actions-title">
        <div className="section-heading"><h2 id="dashboard-actions-title">¿Qué quieres hacer?</h2></div>
        <div className="dashboard-page__actions">
          {dashboardActions.map((action) => <DashboardActionCard key={action.destination} {...action} />)}
        </div>
      </section>

      <section aria-labelledby="recent-activity-title">
        <div className="section-heading dashboard-page__activity-heading">
          <h2 id="recent-activity-title">Actividad reciente</h2>
          <div><Link to="/cotizaciones">Cotizaciones</Link><Link to="/polizas">Pólizas</Link><Link to="/pagos">Pagos</Link></div>
        </div>
        {status === 'loading' && <p className="dashboard-page__feedback" role="status">Consultando actividad…</p>}
        {status === 'error' && (
          <div className="dashboard-page__feedback dashboard-page__feedback--error" role="alert">
            <p>{error}</p>
            <button className="button button--primary" type="button" onClick={retry}>Reintentar</button>
          </div>
        )}
        {status === 'ready' && <ActivityList items={panel.recentActivity} />}
      </section>
    </div>
  )
}

export default DashboardPage
