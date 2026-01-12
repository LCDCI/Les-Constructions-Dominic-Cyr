import axios from 'axios';

const axiosInstance = axios.create({
    baseURL: 'http://localhost:8080/api/v1',
    headers: {
        'Content-Type': 'application/json',
    },
    timeout: 10000,
});

// Pick audience from env (Vite uses import.meta.env, CRA uses process.env)
// Replace with whatever your app builder provides; VITE_ prefix is common in this repo (Vite).
const AUTH0_AUDIENCE = import.meta?.env?.VITE_AUTH0_AUDIENCE || process.env.REACT_APP_AUTH0_AUDIENCE || 'https://construction-api.loca';

export const setupAxiosInterceptors = (getAccessTokenSilently) => {
    axiosInstance.interceptors.request.use(
        async (config) => {
            try {
                if (typeof getAccessTokenSilently === 'function') {
                    const token = await getAccessTokenSilently({
                        authorizationParams: {
                            audience: AUTH0_AUDIENCE,
                        },
                    });
                    if (token) {
                        config.headers = config.headers || {};
                        config.headers.Authorization = `Bearer ${token}`;
                        console.debug('Axios: added Authorization header');
                    } else {
                        console.debug('Axios: no token received from getAccessTokenSilently');
                    }
                } else {
                    console.debug('Axios: getAccessTokenSilently is not a function');
                }
            } catch (error) {
                console.error('Could not get Auth0 token', error);
            }
            return config;
        },
        (error) => Promise.reject(error)
    );

    // Optional: add response interceptor for clearer logs
    axiosInstance.interceptors.response.use(
        (response) => response,
        (error) => {
            if (error.response) {
                console.error('Axios response error', {
                    url: error.config?.url,
                    status: error.response.status,
                    data: error.response.data,
                });
            } else {
                console.error('Axios error', error.message);
            }
            return Promise.reject(error);
        }
    );
};

export default axiosInstance;