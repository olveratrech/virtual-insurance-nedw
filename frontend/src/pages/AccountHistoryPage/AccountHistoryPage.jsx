import { useEffect, useState } from 'react'
import ActivityList from '../../components/dashboard/ActivityList/ActivityList.jsx'
import { getAccountHistory } from '../../services/panelApi.js'
import './AccountHistoryPage.css'

function AccountHistoryPage({ eyebrow, title, description, historyKey, emptyMessage }) {
  const [items, setItems] = useState([])
  const [status, setStatus] = useState('loading')
  const [error, setError] = useState('')
  const [reloadKey, setReloadKey] = useState(0)
  const [query, setQuery] = useState('')
  const [statusFilter, setStatusFilter] = useState('TODOS')

  useEffect(() => {
    let active = true
    getAccountHistory()
      .then((history) => {
        if (!active) return
        setItems(history[historyKey])
        setStatus('ready')
        setError('')
      })
      .catch((requestError) => {
        if (!active) return
        setStatus('error')
        setError(requestError.message)
      })
    return () => { active = false }
  }, [historyKey, reloadKey])

  function retry() {
    setStatus('loading')
    setError('')
    setReloadKey((value) => value + 1)
  }

  const availableStatuses = [...new Set(items.map((item) => item.status))]
  const normalizedQuery = query.trim().toLocaleLowerCase('es-MX')
  const visibleItems = items.filter((item) => {
    const matchesStatus = statusFilter === 'TODOS' || item.status === statusFilter
    const searchable = `${item.type} ${item.reference} ${item.detail}`.toLocaleLowerCase('es-MX')
    return matchesStatus && (!normalizedQuery || searchable.includes(normalizedQuery))
  })

  return (
    <div className="account-history-page page-container">
      <header className="account-history-page__heading">
        <p className="eyebrow">{eyebrow}</p>
        <h1>{title}</h1>
        <p>{description}</p>
      </header>

      {status === 'ready' && items.length > 0 && (
        <div className="account-history-page__toolbar" aria-label={`Filtros de ${title.toLowerCase()}`}>
          <label>
            <span>Buscar</span>
            <input type="search" value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Folio, producto o detalle" />
          </label>
          <label>
            <span>Estado</span>
            <select value={statusFilter} onChange={(event) => setStatusFilter(event.target.value)}>
              <option value="TODOS">Todos</option>
              {availableStatuses.map((itemStatus) => <option key={itemStatus} value={itemStatus}>{itemStatus}</option>)}
            </select>
          </label>
        </div>
      )}

      {status === 'loading' && <p role="status">Consultando historial…</p>}
      {status === 'error' && (
        <div className="account-history-page__error" role="alert">
          <p>{error}</p>
          <button className="button button--primary" type="button" onClick={retry}>Reintentar</button>
        </div>
      )}
      {status === 'ready' && items.length === 0 && <p className="account-history-page__empty">{emptyMessage}</p>}
      {status === 'ready' && items.length > 0 && visibleItems.length === 0 && <p className="account-history-page__empty">No hay resultados para los filtros seleccionados.</p>}
      {status === 'ready' && visibleItems.length > 0 && <ActivityList items={visibleItems} />}
    </div>
  )
}

export default AccountHistoryPage
