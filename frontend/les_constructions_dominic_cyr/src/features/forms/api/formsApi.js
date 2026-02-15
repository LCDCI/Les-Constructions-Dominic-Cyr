import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_BASE || '/api/v1';

/**
 * Create and assign a form to a customer
 * @param {Object} payload - Form creation payload
 * @param {string} payload.customerId - User ID (UUID) of the customer
 * @param {string} payload.formType - Type of form (EXTERIOR_DOORS, GARAGE_DOORS, etc.)
 * @param {string} payload.projectIdentifier - Project ID (required)
 * @param {string} payload.lotIdentifier - Lot ID (required)
 * @param {string} token - Auth token
 */
export async function createForm(payload, token) {
  const response = await axios.post(`${API_BASE}/forms`, payload, {
    headers: token ? { Authorization: `Bearer ${token}` } : undefined,
  });
  return response.data;
}

/**
 * Get all forms (filtered by role on backend)
 * @param {string} token - Auth token
 */
export async function getAllForms(token) {
  const response = await axios.get(`${API_BASE}/forms`, {
    headers: token ? { Authorization: `Bearer ${token}` } : undefined,
  });
  return response.data;
}

/**
 * Get forms assigned to current user (customer view)
 * @param {string} token - Auth token
 */
export async function getMyForms(token) {
  const response = await axios.get(`${API_BASE}/forms/my-forms`, {
    headers: token ? { Authorization: `Bearer ${token}` } : undefined,
  });
  return response.data;
}

/**
 * Get a specific form by ID
 * @param {string} formId - Form ID
 * @param {string} token - Auth token
 */
export async function getFormById(formId, token) {
  const response = await axios.get(`${API_BASE}/forms/${formId}`, {
    headers: token ? { Authorization: `Bearer ${token}` } : undefined,
  });
  return response.data;
}

/**
 * Update form data (customer edit)
 * @param {string} formId - Form ID
 * @param {Object} payload - Form data update payload
 * @param {Object} payload.formData - Updated form data
 * @param {string} token - Auth token
 */
/**
 * Update form data (used by customers filling out forms)
 * @param {string} formId - Form ID
 * @param {Object} payload - Update payload with formData and optional isSubmitting flag
 * @param {string} token - Auth token
 */
export async function updateFormData(formId, payload, token) {
  const response = await axios.put(
    `${API_BASE}/forms/${formId}/data`,
    payload,
    {
      headers: token ? { Authorization: `Bearer ${token}` } : undefined,
    }
  );
  return response.data;
}

/**
 * Submit a form
 * @param {string} formId - Form ID
 * @param {Object} payload - Submission payload
 * @param {Object} payload.formData - Form data to submit
 * @param {boolean} payload.isSubmitting - Should be true
 * @param {string} token - Auth token
 */
export async function submitForm(formId, payload, token) {
  const response = await axios.post(
    `${API_BASE}/forms/${formId}/submit`,
    payload,
    {
      headers: token ? { Authorization: `Bearer ${token}` } : undefined,
    }
  );
  return response.data;
}

/**
 * Reopen a form (salesperson/owner action)
 * @param {string} formId - Form ID
 * @param {Object} payload - Reopen payload
 * @param {string} payload.reason - Reason for reopening
 * @param {string} token - Auth token
 */
export async function reopenForm(formId, payload, token) {
  const response = await axios.post(
    `${API_BASE}/forms/${formId}/reopen`,
    payload,
    {
      headers: token ? { Authorization: `Bearer ${token}` } : undefined,
    }
  );
  return response.data;
}

/**
 * Complete a form (salesperson/owner action)
 * @param {string} formId - Form ID
 * @param {string} token - Auth token
 */
export async function completeForm(formId, token) {
  const response = await axios.post(
    `${API_BASE}/forms/${formId}/complete`,
    {},
    {
      headers: token ? { Authorization: `Bearer ${token}` } : undefined,
    }
  );
  return response.data;
}

/**
 * Delete a form
 * @param {string} formId - Form ID
 * @param {string} token - Auth token
 */
export async function deleteForm(formId, token) {
  const response = await axios.delete(`${API_BASE}/forms/${formId}`, {
    headers: token ? { Authorization: `Bearer ${token}` } : undefined,
  });
  return response.data;
}

/**
 * Get submission history for a form
 * @param {string} formId - Form ID
 * @param {string} token - Auth token
 */
export async function getFormHistory(formId, token) {
  const response = await axios.get(`${API_BASE}/forms/${formId}/history`, {
    headers: token ? { Authorization: `Bearer ${token}` } : undefined,
  });
  return response.data;
}

/**
 * Get forms for a lot (optionally filtered by status)
 * @param {string} lotId - Lot ID
 * @param {Object} options - Optional filters
 * @param {string} options.status - Optional status filter
 * @param {string} token - Auth token
 */
export async function getFormsByLot(lotId, options = {}, token) {
  const params = new URLSearchParams();
  if (options?.status) {
    params.append('status', options.status);
  }

  const response = await axios.get(
    `${API_BASE}/forms/lot/${lotId}${params.toString() ? `?${params}` : ''}`,
    {
      headers: token ? { Authorization: `Bearer ${token}` } : undefined,
    }
  );
  return response.data;
}

/**
 * Download a finalized form as PDF
 * @param {string} formId - Form ID
 * @param {string} token - Auth token
 */
export async function downloadFinalizedForm(formId, token) {
  const response = await axios.get(`${API_BASE}/forms/${formId}/download`, {
    headers: token ? { Authorization: `Bearer ${token}` } : undefined,
    responseType: 'blob',
  });

  const contentDisposition = response.headers['content-disposition'] || '';
  const fileNameMatch = contentDisposition.match(/filename="?([^";]+)"?/i);
  const fileName = fileNameMatch?.[1] || `form_${formId}.pdf`;

  const blob = new Blob([response.data], {
    type: response.headers['content-type'] || 'application/pdf',
  });
  const downloadUrl = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = downloadUrl;
  link.download = fileName;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(downloadUrl);
}
