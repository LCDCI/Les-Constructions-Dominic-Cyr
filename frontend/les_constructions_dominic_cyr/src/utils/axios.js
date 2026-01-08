import axios from 'axios';

const axiosInstance = axios.create({
    baseURL: 'http://localhost:8080/api/v1',
    headers: {
        'Content-Type': 'application/json',
    },
    timeout: 10000,
});

export const setupAxiosInterceptors = (getAccessTokenSilently) => {
    axiosInstance.interceptors.request.use(
        async (config) => {
            try {
                const token = await getAccessTokenSilently({
                    authorizationParams: {
                        audience: 'https://construction-api.loca',
                    },
                });
                if (token) {
                    config.headers.Authorization = `Bearer ${token}`;
                }
            } catch (error) {
                console.error('Could not get Auth0 token', error);
            }
            return config;
        },
        (error) => Promise.reject(error)
    );
};

export default axiosInstance;