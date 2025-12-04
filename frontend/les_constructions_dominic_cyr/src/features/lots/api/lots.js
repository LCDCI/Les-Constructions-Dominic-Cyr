import axios from 'axios'

// Vite exposes env vars as import.meta.env.VITE_*
const BASE = import.meta.env.VITE_API_BASE || '/api/v1/lots'

const api = axios.create({
  baseURL: BASE,
  timeout: 7000,
  headers: { 'Content-Type': 'application/json' },
})

export async function fetchLots() {
  const res = await api.get('/')
  return res.data || []
}