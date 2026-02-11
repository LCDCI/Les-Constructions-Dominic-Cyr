import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_BASE || '/api/v1';

/**
 * Create and assign a form to a customer
 * @param {Object} payload - Form creation payload
 * @param {string} payload.customerId - User ID (UUID) of the customer
 * @param {string} payload.formType - Type of form (EXTERIOR_DOORS, GARAGE_DOORS, etc.)
 * @param {string} payload.projectIdentifier - Project ID (optional)
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
export async function updateFormData(formId, payload, token) {
  const response = await axios.put(
    `${API_BASE}/forms/${formId}/form-data`,
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
 * @param {string} token - Auth token
 */
export async function submitForm(formId, token) {
  const response = await axios.post(
    `${API_BASE}/forms/${formId}/submit`,
    {},
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
