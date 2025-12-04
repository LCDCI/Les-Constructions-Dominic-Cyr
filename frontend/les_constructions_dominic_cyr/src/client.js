import axios from 'axios'

// Read base from Vite env var or default to API root
const RAW_BASE = import.meta.env.VITE_API_BASE || '/api/v1'

function normalizeBase(raw) {
  if (!raw) return '/api/v1'
  let s = String(raw).trim()
  if (s.startsWith('http://') || s.startsWith('https://')) {
    return s.endsWith('/') && s !== '/' ? s.slice(0, -1) : s
  }
  if (!s.startsWith('/')) s = '/' + s
  if (s !== '/' && s.endsWith('/')) s = s.slice(0, -1)
  return s
}

export const API_BASE = normalizeBase(RAW_BASE)
console.info('API base:', API_BASE)

const api = axios.create({
  baseURL: API_BASE,
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' },
})

export default api