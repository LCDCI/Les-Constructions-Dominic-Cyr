import axios from 'axios';

// Prefer VITE_API_BASE (local), then VITE_API_BASE_URL (prod), fallback to /api/v1
const API_BASE = import.meta.env.VITE_API_BASE || import.meta.env.VITE_API_BASE_URL || '/api/v1';

const apiClient = axios.create({
  baseURL: API_BASE,
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.response.use(
  response => response,
  error => {
    return Promise.reject(error);
  }
);

export default apiClient;
