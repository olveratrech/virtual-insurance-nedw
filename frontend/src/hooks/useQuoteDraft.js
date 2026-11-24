import { useEffect, useState } from 'react'
import { getQuote } from '../services/quoteApi.js'

export function useQuoteDraft(quoteId, type) {
  const [result, setResult] = useState(null)

  useEffect(() => {
    if (!quoteId) return
    let active = true
    getQuote(quoteId).then((quote) => {
      if (!active) return
      setResult({ quoteId, values: quote.type === type ? quote.details : undefined, error: quote.type !== type })
    }).catch(() => {
      if (active) setResult({ quoteId, error: true })
    })
    return () => { active = false }
  }, [quoteId, type])

  if (!quoteId) return { loading: false }
  if (result?.quoteId !== quoteId) return { loading: true }
  return { loading: false, values: result.values, error: result.error }
}
