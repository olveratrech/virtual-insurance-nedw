class ApiError extends Error {
  constructor(message, status, problem = null) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.problem = problem
  }
}

let csrf = null

async function request(path, options = {}) {
  const response = await fetch(path, {
    credentials: 'include',
    ...options,
  })

  if (response.status === 204) return null

  const contentType = response.headers.get('content-type') ?? ''
  const body = contentType.includes('json')
    ? await response.json()
    : null

  if (!response.ok) {
    const message = body?.detail ?? 'No fue posible completar la solicitud.'
    throw new ApiError(message, response.status, body)
  }

  return body
}

async function getCsrf() {
  if (csrf) return csrf
  csrf = await request('/auth/csrf')
  return csrf
}

async function authenticatedRequest(path, options = {}) {
  const csrfData = await getCsrf()

  return request(path, {
    ...options,
    headers: {
      ...options.headers,
      [csrfData.headerName]: csrfData.token,
    },
  })
}

export async function getCurrentSession() {
  try {
    return await request('/auth/me')
  } catch (error) {
    if (error instanceof ApiError && error.status === 401) return null
    throw error
  }
}

export function login(credentials) {
  return authenticatedRequest('/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(credentials),
  })
}

export async function logout() {
  try {
    await authenticatedRequest('/auth/logout', { method: 'POST' })
  } finally {
    csrf = null
  }
}
