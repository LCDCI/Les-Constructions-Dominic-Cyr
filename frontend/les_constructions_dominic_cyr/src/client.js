import axios from 'axios';

const ERROR_PAGE_PATH = '/error.html';

// Read base from Vite env var or default to API root
// Always use env var or relative path; never fallback to localhost in production
const RAW_BASE = import.meta.env.VITE_API_BASE || '/api/v1';

function normalizeBase(raw) {
  if (!raw) return '/api/v1';
  let s = String(raw).trim();
  if (s.startsWith('http://') || s.startsWith('https://')) {
    return s.endsWith('/') && s !== '/' ? s.slice(0, -1) : s;
  }
  if (!s.startsWith('/')) s = '/' + s;
  if (s !== '/' && s.endsWith('/')) s = s.slice(0, -1);
  return s;
}

export const API_BASE = normalizeBase(RAW_BASE);

// Create axios instance
const api = axios.create({
  baseURL: API_BASE,
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' },
});

// Response interceptor to handle backend failures
// Redirects to static error.html when backend is unavailable
api.interceptors.response.use(
  response => response,
  error => {
    if (error.response) {
      // Handle 502, 503, 504 - Backend unavailable
      if ([502, 503, 504].includes(error.response.status)) {
        // eslint-disable-next-line no-console
        console.error('Backend service unavailable:', error.response.status);
        // Avoid redirect loop if already on error page
        if (window.location.pathname !== ERROR_PAGE_PATH) {
          window.location.href = ERROR_PAGE_PATH;
        }
      }
    } else if (
      error.code !== 'ECONNABORTED' &&
      !error.message?.includes('timeout')
    ) {
      // Network error (not timeout) - backend unreachable
      // eslint-disable-next-line no-console
      console.error('Network error - backend is unreachable');
      // Avoid redirect loop if already on error page
      if (window.location.pathname !== ERROR_PAGE_PATH) {
        window.location.href = ERROR_PAGE_PATH;
      }
    }
    return Promise.reject(error);
  }
);

export default api;
