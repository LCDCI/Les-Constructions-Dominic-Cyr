// src/features/users/api/usersApi.js
import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_BASE || '/api/v1';

/**
 * GET all users
 */
export async function fetchUsers() {
  const response = await axios.get(`${API_BASE}/users`);
  return response.data;
}

/**
 * CREATE a new user
 */
export async function createUser(payload) {
  const response = await axios.post(`${API_BASE}/users`, payload);
  return response.data;
}
