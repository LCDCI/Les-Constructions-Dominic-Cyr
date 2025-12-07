const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

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
