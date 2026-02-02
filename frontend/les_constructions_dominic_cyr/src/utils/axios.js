// src/utils/axios.js
import axios from 'axios';

const ERROR_PAGE_PATH = '/error.html';

const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_APP_API_URL || '/api/v1',
  timeout: 10000,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Export the function that App.jsx is expecting
export const setupAxiosInterceptors = (
  getAccessTokenSilently,
  onUnauthorized
) => {
  axiosInstance.interceptors.request.use(
    async config => {
      try {
        const token = await getAccessTokenSilently();
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
      } catch (error) {
        // no token available
      }
      return config;
    },
    error => Promise.reject(error)
  );

  axiosInstance.interceptors.response.use(
    response => response,
    error => {
      const status = error?.response?.status;
      if (status === 401 && typeof onUnauthorized === 'function') {
        try {
          onUnauthorized();
        } catch (e) {
          // eslint-disable-next-line no-console
          console.error('onUnauthorized handler failed', e);
        }
      }
      return Promise.reject(error);
    }
  );
};

axiosInstance.interceptors.response.use(
  response => response,
  error => {
    if (error.response) {
      // Example: Global handling for unauthorized access
      if (error.response.status === 401) {
        // eslint-disable-next-line no-console
        console.warn('Unauthorized request - check Auth0 session');
      }
      // Handle 502, 503, 504 - Backend unavailable (nginx manages error pages)
      else if ([502, 503, 504].includes(error.response.status)) {
        // eslint-disable-next-line no-console
        console.error('Backend service unavailable:', error.response.status);
      }
    } else {
      // No response received - could be timeout or network error
      if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
        // eslint-disable-next-line no-console
        console.error('Request timeout - backend may be unavailable');
        // Do not redirect for timeouts; allow caller to retry or handle gracefully
      } else {
        // eslint-disable-next-line no-console
        console.error('Network error - backend is unreachable');
        // Redirect to static error page only for non-timeout network failures
        window.location.href = ERROR_PAGE_PATH;
      }
    }
    return Promise.reject(error);
  }
);
export default axiosInstance;
