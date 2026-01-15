// Use relative path to leverage Vite proxy
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api/v1';

export async function fetchLots(token = null) {
  const headers = {};
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}/lots`, { headers });
  if (!response.ok) {
    let message = 'Failed to fetch lots';
    try {
      const errBody = await response.json();
      if (typeof errBody?.message === 'string') message = errBody.message;
      else if (typeof errBody?.error === 'string') message = errBody.error;
    } catch (parseErr) {
      // ignore parse error, keep fallback message
    }
    throw new Error(message);
  }
  return response.json();
}

export async function fetchLotById(id) {
  const response = await fetch(`${API_BASE_URL}/lots/${id}`);
  if (!response.ok) {
    throw new Error('Failed to fetch lot');
  }
  return response.json();
}

export async function createLot(lotData) {
  const response = await fetch(`${API_BASE_URL}/lots`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(lotData),
  });
  if (!response.ok) {
    throw new Error('Failed to create lot');
  }
  return response.json();
}
