const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api/v1';

export async function fetchRenovations() {
  const response = await fetch(`${API_BASE_URL}/renovations`);
  if (!response.ok) {
    let bodyText;
    try {
      bodyText = await response.text();
    } catch (e) {
      bodyText = '<unable to read response body>';
    }
    throw new Error(
      `Failed to fetch renovations: ${response.status} ${response.statusText} - ${bodyText}`
    );
  }
  return response.json();
}

export async function fetchRenovationById(id) {
  const response = await fetch(`${API_BASE_URL}/renovations/${id}`);
  if (!response.ok) {
    let bodyText;
    try {
      bodyText = await response.text();
    } catch (e) {
      bodyText = '<unable to read response body>';
    }
    throw new Error(
      `Failed to fetch renovation ${id}: ${response.status} ${response.statusText} - ${bodyText}`
    );
  }
  return response.json();
}
