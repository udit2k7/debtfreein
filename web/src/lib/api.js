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
    console.error('Error retrieving Firebase ID token:', err);
    return null;
  }
}

/**
 * Helper to execute HTTP requests with automatic Bearer token injection and global error interception.
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

  let response;
  try {
    response = await fetch(url, {
      ...options,
      headers
    });
  } catch (networkErr) {
    const error = new Error('Network connection error. Unable to reach API Gateway.');
    error.status = 0;
    throw error;
  }

  const data = await response.json().catch(() => ({}));

  if (!response.ok) {
    const error = new Error(data.message || `HTTP Error ${response.status}`);
    error.status = response.status;
    error.data = data;

    // 1. Global 401 Unauthorized Interceptor: Purge session and redirect to /login
    if (response.status === 401) {
      console.warn('401 Unauthorized: Session invalid. Purging local state and redirecting to login.');
      try {
        await auth.signOut();
      } catch (e) {
        // ignore signout errors
      }
      if (typeof window !== 'undefined' && !window.location.pathname.startsWith('/login')) {
        window.location.href = '/login?expired=1';
      }
    }

    // 2. Global 403 Forbidden Interceptor: Dispatch subscription forbidden event
    if (response.status === 403) {
      error.isForbidden = true;
      if (typeof window !== 'undefined') {
        window.dispatchEvent(new CustomEvent('gateway-forbidden', { detail: data }));
      }
    }

    // 3. Global 429 Too Many Requests Interceptor: Dispatch rate limit toast event
    if (response.status === 429) {
      error.message = 'Rate limit exceeded. Maximum 60 requests per minute allowed. Please wait a moment.';
      error.isRateLimited = true;
      if (typeof window !== 'undefined') {
        window.dispatchEvent(new CustomEvent('gateway-rate-limited', { detail: { message: error.message } }));
      }
    }

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
