import axios from 'axios'

const apiBase = import.meta.env.VITE_API_BASE ?? '/api/v1'

// Create and export a single axios instance for the app
const api = axios.create({
  baseURL: apiBase,
  headers: { Accept: 'application/json' },
})

export default api