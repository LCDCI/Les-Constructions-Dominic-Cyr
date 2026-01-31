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
  const statusInfo = response?.status ? ` (status ${response.status})` : '';
  return `${message}${statusInfo}`;
};

export const resolveProjectIdentifier = projectIdentifier =>
  projectIdentifier || FALLBACK_PROJECT_IDENTIFIER || null;

export async function fetchLots({ projectIdentifier, token } = {}) {
  const projectId = resolveProjectIdentifier(projectIdentifier);
  const headers = {};
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const url = projectId
    ? `${API_BASE_URL}/projects/${projectId}/lots`
    : `${API_BASE_URL}/lots`;

  const response = await fetch(url, { headers });

  if (!response.ok) {
    const message = await parseErrorMessage(response, 'Failed to fetch lots');
    throw new Error(message);
  }
  return response.json();
}

export async function fetchLotById({ projectIdentifier, lotId, token } = {}) {
  const projectId = resolveProjectIdentifier(projectIdentifier);
  const url = projectId
    ? `${API_BASE_URL}/projects/${projectId}/lots/${lotId}`
    : `${API_BASE_URL}/lots/${lotId}`;

  const headers = {};
  if (token) headers.Authorization = `Bearer ${token}`;

  const response = await fetch(url, { headers });
  if (!response.ok) {
    const message = await parseErrorMessage(response, 'Failed to fetch lot');
    throw new Error(message);
  }
  return response.json();
}

export async function createLot({ projectIdentifier, lotData, token }) {
  const projectId = resolveProjectIdentifier(projectIdentifier);
  if (!projectId) {
    throw new Error('Project identifier is required to create a lot');
  }

  const url = `${API_BASE_URL}/projects/${projectId}/lots`;

  const headers = {
    'Content-Type': 'application/json',
  };

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const payload = {
    ...lotData,
    assignedUserIds: lotData?.assignedUserIds || [],
    projectId,
    projectIdentifier: projectId,
  };

  const response = await fetch(url, {
    method: 'POST',
    headers,
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    const message = await parseErrorMessage(response, 'Failed to create lot');
    throw new Error(message);
  }
  return response.json();
}

export async function updateLot({ projectIdentifier, lotId, lotData, token }) {
  const projectId = resolveProjectIdentifier(projectIdentifier);
  if (!projectId) {
    throw new Error('Project identifier is required to update a lot');
  }
  const url = `${API_BASE_URL}/projects/${projectId}/lots/${lotId}`;

  const headers = {
    'Content-Type': 'application/json',
  };

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const payload = {
    ...lotData,
    assignedUserIds: lotData?.assignedUserIds || [],
    projectId,
    projectIdentifier: projectId,
  };

  const response = await fetch(url, {
    method: 'PUT',
    headers,
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    const message = await parseErrorMessage(response, 'Failed to update lot');
    throw new Error(message);
  }
  return response.json();
}

export async function deleteLot({ projectIdentifier, lotId, token }) {
  const projectId = resolveProjectIdentifier(projectIdentifier);
  if (!projectId) {
    throw new Error('Project identifier is required to delete a lot');
  }
  const url = projectId
    ? `${API_BASE_URL}/projects/${projectId}/lots/${lotId}`
    : `${API_BASE_URL}/lots/${lotId}`;

  const headers = {};

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(url, {
    method: 'DELETE',
    headers,
  });

  if (!response.ok) {
    const message = await parseErrorMessage(response, 'Failed to delete lot');
    throw new Error(message);
  }

  // DELETE typically returns no content (204) or empty response
  if (response.status !== 204) {
    return response.json();
  }
}
