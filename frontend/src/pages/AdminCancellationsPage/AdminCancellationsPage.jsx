import { useEffect, useState } from 'react'
import AdminCancellationList from '../../components/admin/AdminCancellationList/AdminCancellationList.jsx'
import { getAdminCancellations } from '../../services/adminCancellationApi.js'
import './AdminCancellationsPage.css'

const filters = [
  { value: 'SOLICITADA', label: 'Pendientes' },
  { value: 'APROBADA', label: 'Aprobadas' },
  { value: 'RECHAZADA', label: 'Rechazadas' },
]

function AdminCancellationsPage() {
  const [selectedStatus, setSelectedStatus] = useState('SOLICITADA')
  const [items, setItems] = useState([])
  const [status, setStatus] = useState('loading')
  const [error, setError] = useState('')
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    let active = true
    setStatus('loading')
    setError('')
    getAdminCancellations(selectedStatus)
      .then((response) => {
        if (!active) return
        setItems(response.items)
        setStatus('ready')
      })
      .catch((requestError) => {
        if (!active) return
        setError(requestError.message)
        setStatus('error')
      })
    return () => { active = false }
  }, [selectedStatus, reloadKey])

  return (
    <div className="admin-cancellations-page page-container">
      <header>
        <p className="eyebrow">Administración de pólizas</p>
        <h1>Solicitudes de cancelación</h1>
        <p>Consulta las solicitudes y registra una decisión con responsable y observación.</p>
      </header>

      <div className="admin-cancellations-page__filters" role="group" aria-label="Filtrar solicitudes">
        {filters.map((filter) => (
          <button
            className={selectedStatus === filter.value ? 'is-active' : ''}
            type="button"
            key={filter.value}
            aria-pressed={selectedStatus === filter.value}
            onClick={() => setSelectedStatus(filter.value)}
          >
            {filter.label}
          </button>
        ))}
      </div>

      {status === 'loading' && <p role="status">Consultando solicitudes…</p>}
      {status === 'error' && (
        <div className="admin-cancellations-page__error" role="alert">
          <p>{error}</p>
          <button className="button button--primary" type="button" onClick={() => setReloadKey((value) => value + 1)}>Reintentar</button>
        </div>
      )}
      {status === 'ready' && <AdminCancellationList items={items} />}
    </div>
  )
}

export default AdminCancellationsPage
