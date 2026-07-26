import { auth } from './firebase';

const BASE_URL = import.meta.env.VITE_API_GATEWAY_URL || 'https://us-central1-debtfreein-app.cloudfunctions.net/api';

/**
 * Gets the current user's Firebase ID Token.
 */
async function getAuthToken() {
  const user = auth.currentUser;
  if (!user) return null;
  try {
    return await user.getIdToken();
  } catch (err) {
    console.error('Error getting ID token:', err);
    return null;
  }
}

/**
 * Helper to execute HTTP requests with automatic Bearer token injection.
 */
async function request(endpoint, options = {}) {
  const token = await getAuthToken();
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {})
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const cleanEndpoint = endpoint.startsWith('/') ? endpoint : `/${endpoint}`;
  const url = endpoint.startsWith('http') ? endpoint : `${BASE_URL}${cleanEndpoint}`;

  const response = await fetch(url, {
    ...options,
    headers
  });

  const data = await response.json().catch(() => ({}));

  if (!response.ok) {
    const error = new Error(data.message || `HTTP Error ${response.status}`);
    error.status = response.status;
    error.data = data;
    throw error;
  }

  return data;
}

export const api = {
  get: (endpoint, options = {}) => request(endpoint, { ...options, method: 'GET' }),
  post: (endpoint, body = {}, options = {}) =>
    request(endpoint, { ...options, method: 'POST', body: JSON.stringify(body) }),
  put: (endpoint, body = {}, options = {}) =>
    request(endpoint, { ...options, method: 'PUT', body: JSON.stringify(body) }),
  delete: (endpoint, options = {}) => request(endpoint, { ...options, method: 'DELETE' })
};

export default api;
