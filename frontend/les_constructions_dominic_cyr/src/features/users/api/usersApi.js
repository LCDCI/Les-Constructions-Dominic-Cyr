// src/features/users/api/usersApi.js
import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_BASE || '/api/v1';

/**
 * GET all users
 */
export async function fetchUsers(token) {
  const response = await axios.get(`${API_BASE}/users`, {
    headers: token ? { Authorization: `Bearer ${token}` } : undefined,
  });
  return response.data;
}

/**
 * CREATE a new user
 */
export async function createUser(payload, token) {
  const response = await axios.post(`${API_BASE}/users`, payload, {
    headers: token ? { Authorization: `Bearer ${token}` } : undefined,
  });
  return response.data;
}

/**
 * GET a user by ID
 */
export async function fetchUserById(userId, token) {
  const response = await axios.get(`${API_BASE}/users/${userId}`, {
    headers: token ? { Authorization: `Bearer ${token}` } : undefined,
  });
  return response.data;
}

/**
 * GET a user by Auth0 ID
 */
export async function fetchUserByAuth0Id(auth0UserId, token) {
  if (!auth0UserId) {
    throw new Error('Auth0 user ID is required');
  }

  const response = await axios.get(`${API_BASE}/users/auth0/${auth0UserId}`, {
    headers: token ? { Authorization: `Bearer ${token}` } : undefined,
  });
  return response.data;
}

/**
 * UPDATE a user
 */
export async function updateUser(userId, payload, token) {
  const response = await axios.put(`${API_BASE}/users/${userId}`, payload, {
    headers: token ? { Authorization: `Bearer ${token}` } : undefined,
  });
  return response.data;
}
