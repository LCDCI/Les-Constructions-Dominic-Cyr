import axios from 'axios';
import { navigate } from './utils/navigation';

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

// Response interceptor to handle 500 errors
api.interceptors.response.use(
  response => {
    // If response is successful, return it as-is
    return response;
  },
  error => {
    // Handle error responses
    if (error.response) {
      const status = error.response.status;

      // Redirect to error page on 500 status codes
      if (status >= 500 && status < 600) {
        navigate('/error', { replace: true });
      }
    } else if (error.request) {
      // Request was made but no response received (network error)
      // You can handle this differently if needed
    } else {
      // Something else happened
    }

    // Reject the promise so components can still handle errors if needed
    return Promise.reject(error);
  }
);

export default api;
