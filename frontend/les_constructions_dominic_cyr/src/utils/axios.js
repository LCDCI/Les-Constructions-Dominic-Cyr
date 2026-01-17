// src/utils/axios.js
import axios from 'axios';

const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_APP_API_URL || 'http://localhost:8080/api/v1',
  timeout: 10000,
  withCredentials: true,
});

// Export the function that App.jsx is expecting
export const setupAxiosInterceptors = (getAccessTokenSilently, onUnauthorized) => {
  axiosInstance.interceptors.request.use(
    async config => {
      try {
        const token = await getAccessTokenSilently();
        if (token) config.headers.Authorization = `Bearer ${token}`;
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
      if ((status === 401 || status === 403) && typeof onUnauthorized === 'function') {
        try {
          onUnauthorized();
        } catch (e) {
          console.error('onUnauthorized handler failed', e);
        }
      }
      return Promise.reject(error);
    }
  );
};

export default axiosInstance;
