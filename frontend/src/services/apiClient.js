export class ApiError extends Error {
  constructor(message, status, problem = null) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.problem = problem
  }
}

export async function apiRequest(path, options = {}) {
  let response

  try {
    response = await fetch(path, {
      credentials: 'include',
      cache: 'no-store',
      ...options,
    })
  } catch {
    throw new ApiError('No fue posible conectar con el servidor.', 0)
  }

  if (response.status === 204) return null

  const contentType = response.headers.get('content-type') ?? ''
  const body = contentType.includes('json')
    ? await response.json()
    : null

  if (!response.ok) {
    const fallbackMessages = {
      401: 'La sesión no es válida o ha expirado.',
      403: 'La solicitud fue rechazada por la protección de seguridad.',
      404: 'El recurso solicitado no existe.',
      500: 'El servidor encontró un error al procesar la solicitud.',
    }
    const message = body?.detail
      ?? fallbackMessages[response.status]
      ?? `No fue posible completar la solicitud (HTTP ${response.status}).`
    throw new ApiError(message, response.status, body)
  }

  return body
}
