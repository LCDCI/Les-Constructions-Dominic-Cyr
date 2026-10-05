const API_BASE_URL = import.meta.env.VITE_API_BASE || '/api/v1';

async function requestWithRetry(url, options = {}, maxAttempts = 3) {
  let lastError;

  for (let attempt = 0; attempt < maxAttempts; attempt += 1) {
    try {
      const response = await fetch(url, options);
      if (response.ok) return response;

      const error = new Error(
        `Site content request failed (${response.status})`
      );
      error.status = response.status;
      throw error;
    } catch (error) {
      lastError = error;
    }
  }

  throw lastError;
}

export async function fetchSiteContentGroups(language, token) {
  const params = new URLSearchParams({ language });
  const response = await requestWithRetry(
    `${API_BASE_URL}/site-content/groups?${params.toString()}`,
    { headers: { Authorization: `Bearer ${token}` } }
  );
  return response.json();
}

export async function fetchSiteContent(language, token, pageGroup) {
  const params = new URLSearchParams({ language, pageGroup });
  const response = await requestWithRetry(
    `${API_BASE_URL}/site-content?${params.toString()}`,
    { headers: { Authorization: `Bearer ${token}` } }
  );
  return response.json();
}

export async function updateSiteContent(
  id,
  contentText,
  imageIdentifier,
  token
) {
  const response = await requestWithRetry(
    `${API_BASE_URL}/site-content/${id}`,
    {
      method: 'PUT',
      headers: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ contentText, imageIdentifier }),
    }
  );
  return response.json();
}

export async function createSiteContent(data, token) {
  const response = await requestWithRetry(`${API_BASE_URL}/site-content`, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(data),
  });
  return response.json();
}

export async function deleteSiteContent(id, token) {
  await requestWithRetry(`${API_BASE_URL}/site-content/${id}`, {
    method: 'DELETE',
    headers: { Authorization: `Bearer ${token}` },
  });
}
