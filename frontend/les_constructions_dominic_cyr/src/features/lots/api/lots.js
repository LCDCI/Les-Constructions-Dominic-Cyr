// Use relative path to leverage Vite proxy
const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || '/api/v1';

export async function fetchLots() {
  const response = await fetch(`${API_BASE_URL}/lots`);
  if (!response.ok) {
    throw new Error('Failed to fetch lots');
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