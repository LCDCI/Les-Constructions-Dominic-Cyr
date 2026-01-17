// Use relative path to leverage Vite proxy
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api/v1';

const FALLBACK_PROJECT_IDENTIFIER =
  import.meta.env.VITE_PUBLIC_PROJECT_IDENTIFIER ||
  import.meta.env.VITE_DEFAULT_PROJECT_IDENTIFIER ||
  import.meta.env.VITE_PROJECT_IDENTIFIER;

const parseErrorMessage = async (response, fallback) => {
  let message = fallback;
  try {
    const body = await response.json();
    if (typeof body?.message === 'string') message = body.message;
    else if (typeof body?.error === 'string') message = body.error;
  } catch (err) {
    // keep fallback message
  }
  return message;
};

export const resolveProjectIdentifier = projectIdentifier => {
  const resolved = projectIdentifier || FALLBACK_PROJECT_IDENTIFIER;
  if (!resolved) {
    throw new Error('Project identifier is required to call the lots API');
  }
  return resolved;
};

export async function fetchLots({ projectIdentifier, token } = {}) {
  const projectId = resolveProjectIdentifier(projectIdentifier);
  const headers = {};
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}/projects/${projectId}/lots`, {
    headers,
  });

  if (!response.ok) {
    const message = await parseErrorMessage(response, 'Failed to fetch lots');
    throw new Error(message);
  }
  return response.json();
}

export async function fetchLotById({ projectIdentifier, lotId }) {
  const projectId = resolveProjectIdentifier(projectIdentifier);
  const response = await fetch(
    `${API_BASE_URL}/projects/${projectId}/lots/${lotId}`
  );
  if (!response.ok) {
    const message = await parseErrorMessage(response, 'Failed to fetch lot');
    throw new Error(message);
  }
  return response.json();
}

export async function createLot({ projectIdentifier, lotData }) {
  const projectId = resolveProjectIdentifier(projectIdentifier);
  const response = await fetch(`${API_BASE_URL}/projects/${projectId}/lots`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(lotData),
  });
  if (!response.ok) {
    const message = await parseErrorMessage(response, 'Failed to create lot');
    throw new Error(message);
  }
  return response.json();
}
