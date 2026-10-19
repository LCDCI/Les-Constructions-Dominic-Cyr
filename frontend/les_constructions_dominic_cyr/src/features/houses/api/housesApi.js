const API_BASE_URL = import.meta.env.VITE_API_BASE || '/api/v1';

async function request(url, options = {}) {
  const response = await fetch(url, options);
  if (!response.ok) {
    const error = new Error(`House request failed (${response.status})`);
    error.status = response.status;
    throw error;
  }
  if (response.status === 204) return null;
  return response.json();
}

const authHeaders = token =>
  token ? { Authorization: `Bearer ${token}` } : {};

export function fetchHouses(projectIdentifier, token) {
  const query = projectIdentifier
    ? `?projectIdentifier=${encodeURIComponent(projectIdentifier)}`
    : '';
  return request(`${API_BASE_URL}/houses${query}`, {
    headers: authHeaders(token),
  });
}

export function createHouse(house, token) {
  return request(`${API_BASE_URL}/houses`, {
    method: 'POST',
    headers: { ...authHeaders(token), 'Content-Type': 'application/json' },
    body: JSON.stringify(house),
  });
}

export function updateHouse(houseIdentifier, house, token) {
  return request(`${API_BASE_URL}/houses/${houseIdentifier}`, {
    method: 'PUT',
    headers: { ...authHeaders(token), 'Content-Type': 'application/json' },
    body: JSON.stringify(house),
  });
}

export async function uploadHouseImage(houseIdentifier, file, token) {
  const formData = new FormData();
  formData.append('file', file);
  const response = await fetch(
    `${API_BASE_URL}/houses/${houseIdentifier}/image`,
    {
      method: 'POST',
      headers: token ? { Authorization: `Bearer ${token}` } : {},
      body: formData,
    }
  );
  if (!response.ok) {
    throw new Error(`House image upload failed (${response.status})`);
  }
  return response.json();
}

export function deleteHouse(houseIdentifier, token) {
  return request(`${API_BASE_URL}/houses/${houseIdentifier}`, {
    method: 'DELETE',
    headers: authHeaders(token),
  });
}
