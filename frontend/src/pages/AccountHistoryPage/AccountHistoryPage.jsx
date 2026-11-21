import { useEffect, useState } from 'react'
import ActivityList from '../../components/dashboard/ActivityList/ActivityList.jsx'
import { getAccountHistory } from '../../services/panelApi.js'
import './AccountHistoryPage.css'

function AccountHistoryPage({ eyebrow, title, description, historyKey, emptyMessage }) {
  const [items, setItems] = useState([])
  const [status, setStatus] = useState('loading')
  const [error, setError] = useState('')
  const [reloadKey, setReloadKey] = useState(0)

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

  return (
    <div className="account-history-page page-container">
      <header className="account-history-page__heading">
        <p className="eyebrow">{eyebrow}</p>
        <h1>{title}</h1>
        <p>{description}</p>
      </header>

      {status === 'loading' && <p role="status">Consultando historial…</p>}
      {status === 'error' && (
        <div className="account-history-page__error" role="alert">
          <p>{error}</p>
          <button className="button button--primary" type="button" onClick={retry}>Reintentar</button>
        </div>
      )}
      {status === 'ready' && items.length === 0 && <p className="account-history-page__empty">{emptyMessage}</p>}
      {status === 'ready' && items.length > 0 && <ActivityList items={items} />}
    </div>
  )
}

export default AccountHistoryPage
