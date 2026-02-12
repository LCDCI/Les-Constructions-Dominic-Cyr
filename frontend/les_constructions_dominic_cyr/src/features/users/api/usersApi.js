import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_BASE || '/api/v1';

export async function fetchUsers(token) {
  const response = await axios.get(`${API_BASE}/users`, {
    headers: token ? { Authorization: `Bearer ${token}` } : undefined,
  });
  return response.data;
}

export async function createUser(payload, token) {
  const response = await axios.post(`${API_BASE}/users`, payload, {
    headers: token ? { Authorization: `Bearer ${token}` } : undefined,
  });
  return response.data;
}

export async function fetchUserById(userId, token) {
  const response = await axios.get(`${API_BASE}/users/${userId}`, {
    headers: token ? { Authorization: `Bearer ${token}` } : undefined,
  });
  return response.data;
}

export async function fetchUserByAuth0Id(auth0UserId, token) {
  if (!auth0UserId) {
    throw new Error('Auth0 user ID is required');
  }

  const response = await axios.get(`${API_BASE}/users/auth0/${auth0UserId}`, {
    headers: token ? { Authorization: `Bearer ${token}` } : undefined,
  });
  return response.data;
}

export async function updateUser(userId, payload, token) {
  const response = await axios.put(`${API_BASE}/users/${userId}`, payload, {
    headers: token ? { Authorization: `Bearer ${token}` } : undefined,
  });
  return response.data;
}

export async function updateUserAsOwner(userId, payload, token) {
  const response = await axios.put(
    `${API_BASE}/users/${userId}/owner`,
    payload,
    {
      headers: token ? { Authorization: `Bearer ${token}` } : undefined,
    }
  );
  return response.data;
}

export async function getCurrentUser(token) {
  const response = await axios.get(`${API_BASE}/users/me`, {
    headers: token ? { Authorization: `Bearer ${token}` } : undefined,
  });
  return response.data;
}

export async function deactivateUser(userId, token) {
  const response = await axios.patch(
    `${API_BASE}/users/${userId}/deactivate`,
    {},
    {
      headers: token ? { Authorization: `Bearer ${token}` } : undefined,
    }
  );
  return response.data;
}
export async function fetchActiveContractors(token) {
  const response = await axios.get(`${API_BASE}/users/contractors/active`, {
    headers: token ? { Authorization: `Bearer ${token}` } : undefined,
  });
  return response.data;
}

export async function fetchActiveSalespersons(token) {
  const response = await axios.get(`${API_BASE}/users/salespersons/active`, {
    headers: token ? { Authorization: `Bearer ${token}` } : undefined,
  });
  return response.data;
}

export async function fetchActiveCustomers(token) {
  const response = await axios.get(`${API_BASE}/users/customers/active`, {
    headers: token ? { Authorization: `Bearer ${token}` } : undefined,
  });
  return response.data;
}

export async function fetchCustomersWithSharedLots(token) {
  const response = await axios.get(`${API_BASE}/users/customers/shared-lots`, {
    headers: token ? { Authorization: `Bearer ${token}` } : undefined,
  });
  return response.data;
}

export async function fetchAllContractors(token) {
  const response = await axios.get(`${API_BASE}/users`, {
    headers: token ? { Authorization: `Bearer ${token}` } : undefined,
  });
  const allUsers = response.data;
  return allUsers.filter(user => user.userRole === 'CONTRACTOR');
}

export async function fetchAllSalespersons(token) {
  const response = await axios.get(`${API_BASE}/users`, {
    headers: token ? { Authorization: `Bearer ${token}` } : undefined,
  });
  const allUsers = response.data;
  return allUsers.filter(user => user.userRole === 'SALESPERSON');
}

export async function fetchAllCustomers(token) {
  const response = await axios.get(`${API_BASE}/users`, {
    headers: token ? { Authorization: `Bearer ${token}` } : undefined,
  });
  const allUsers = response.data;
  return allUsers.filter(user => user.userRole === 'CUSTOMER');
}

export async function setUserInactive(userId, token) {
  const response = await axios.patch(
    `${API_BASE}/users/${userId}/inactive`,
    {},
    {
      headers: token ? { Authorization: `Bearer ${token}` } : undefined,
    }
  );
  return response.data;
}

export async function reactivateUser(userId, token) {
  const response = await axios.patch(
    `${API_BASE}/users/${userId}/reactivate`,
    {},
    {
      headers: token ? { Authorization: `Bearer ${token}` } : undefined,
    }
  );
  return response.data;
}
