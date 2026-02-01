// Lot Documents API Client
import axios from 'axios';

const BASE_API_URL =
  import.meta.env.VITE_BACKEND_URL ||
  (typeof window !== 'undefined' ? `${window.location.origin}/api/v1` : '/api/v1');

/**
 * Get all documents for a lot with optional filtering
 * @param {string} lotId - The lot identifier
 * @param {Object} options - Filter options: { search: string, type: 'all'|'image'|'file' }
 * @param {string} token - Auth token
 * @returns {Promise<Array>} - Array of lot documents
 */
export async function fetchLotDocuments(lotId, options = {}, token = null) {
  const { search, type = 'all' } = options;
  let url = `${BASE_API_URL}/lots/${lotId}/documents?type=${type}`;
  
  if (search) {
    url += `&search=${encodeURIComponent(search)}`;
  }

  const headers = token ? { Authorization: `Bearer ${token}` } : {};
  const response = await axios.get(url, { headers });
  return response.data;
}

/**
 * Upload one or more documents to a lot
 * @param {string} lotId - The lot identifier
 * @param {FileList|File[]} files - Files to upload
 * @param {string} token - Auth token
 * @returns {Promise<Array>} - Array of created lot documents
 */
export async function uploadLotDocuments(lotId, files, token) {
  const formData = new FormData();
  
  // Append all files
  if (files instanceof FileList) {
    for (let i = 0; i < files.length; i++) {
      formData.append('files', files[i]);
    }
  } else if (Array.isArray(files)) {
    files.forEach((file) => {
      formData.append('files', file);
    });
  } else {
    formData.append('files', files);
  }

  const headers = {
    'Content-Type': 'multipart/form-data',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  };

  const response = await axios.post(
    `${BASE_API_URL}/lots/${lotId}/documents`,
    formData,
    { headers }
  );
  return response.data;
}

/**
 * Download a document from a lot
 * @param {string} lotId - The lot identifier
 * @param {string} documentId - The document ID
 * @param {string} fileName - The file name for download
 * @param {string} token - Auth token
 * @returns {Promise<void>} - Triggers browser download
 */
export async function downloadLotDocument(lotId, documentId, fileName, token) {
  const headers = token ? { Authorization: `Bearer ${token}` } : {};
  
  const response = await axios.get(
    `${BASE_API_URL}/lots/${lotId}/documents/${documentId}/download`,
    {
      headers,
      responseType: 'blob',
    }
  );

  // Trigger browser download
  const blob = new Blob([response.data]);
  const downloadUrl = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = downloadUrl;
  link.download = fileName || 'download';
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(downloadUrl);
}

/**
 * Delete a document from a lot
 * @param {string} lotId - The lot identifier
 * @param {string} documentId - The document ID
 * @param {string} token - Auth token
 * @returns {Promise<Object>} - Response message
 */
export async function deleteLotDocument(lotId, documentId, token) {
  const headers = token ? { Authorization: `Bearer ${token}` } : {};
  
  const response = await axios.delete(
    `${BASE_API_URL}/lots/${lotId}/documents/${documentId}`,
    { headers }
  );
  return response.data;
}
