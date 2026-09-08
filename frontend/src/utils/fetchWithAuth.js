export async function fetchWithAuth(url, options = {}) {
  // options.handleUnauthorized: boolean (default true) - whether helper should auto-clear token+redirect on 401/403
  const handleUnauthorized = options.handleUnauthorized !== false
  const token = sessionStorage.getItem('authToken')
  const headers = Object.assign({}, options.headers || {})
  if (token) headers['Authorization'] = 'Bearer ' + token

  // Create a shallow copy of options so we don't leak the helper-specific flag to fetch
  const { handleUnauthorized: _hu, ...fetchOptions } = options
  const res = await fetch(url, Object.assign({}, fetchOptions, { headers }))

  if (handleUnauthorized && (res.status === 401 || res.status === 403)) {
    try {
      sessionStorage.removeItem('authToken')
    } catch (e) {
      // ignore
    }
    // Force navigation to auth page to show login UI
    if (typeof window !== 'undefined') {
      try { window.location.href = '/auth' } catch (e) { window.location.reload() }
    }
    const text = await res.text().catch(() => '')
    throw new Error('Unauthorized: ' + text)
  }

  return res
}
