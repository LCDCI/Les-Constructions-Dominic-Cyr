// src/utils/axios.js
import axios from 'axios';

const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_APP_API_URL || 'http://localhost:8080/api/v1',
  timeout: 10000,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Export the function that App.jsx is expecting
export const setupAxiosInterceptors = getAccessTokenSilently => {
  axiosInstance.interceptors.request.use(
    async config => {
      try {
        const token = await getAccessTokenSilently();
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
      } catch (error) {
        console.error('Error getting access token:', error);
      }
      return config;
    },
    error => Promise.reject(error)
  );
};

axiosInstance.interceptors.response.use(
  response => response,
  error => {
    if (error.response) {
      // Example: Global handling for unauthorized access
      if (error.response.status === 401) {
        console.warn('Unauthorized request - check Auth0 session');
      }
    }
    return Promise.reject(error);
  }
);
export default axiosInstance;
