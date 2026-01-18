import axios from 'axios';

const BASE_API_URL = import.meta.env.VITE_FILES_SERVICE_URL || 'http://localhost:8082';

// Archive a file (photo)
export async function archiveFile(fileId, { archivedBy }) {
    const archivedByValue = String(archivedBy);
    const response = await axios.post(`${BASE_API_URL}/files/${fileId}/archive`, { archivedBy: archivedByValue }, {
        headers: { 'Content-Type': 'application/json' }
    });
    return response.data;
}

// Unarchive a file (photo)
export async function unarchiveFile(fileId) {
    const response = await axios.post(`${BASE_API_URL}/files/${fileId}/unarchive`, null, {
        headers: { 'Content-Type': 'application/json' }
    });
    return response.data;
}

export async function fetchProjectFiles(projectId, options = {}) {
    const archived = options.archived === true;
    const url = `${BASE_API_URL}/projects/${projectId}/files${archived ? '?archived=true' : ''}`;
    const response = await axios.get(url);
    return response.data;
}

export async function fetchProjectDocuments(projectId) {
    const response = await axios.get(`${BASE_API_URL}/projects/${projectId}/documents`); 
    return response.data;
}

export async function uploadFile(formData) {
    const response = await axios.post(`${BASE_API_URL}/files`, formData, {
        headers: {
            'Content-Type': 'multipart/form-data',
        },
    });
    return response.data;
}

export async function deleteFile(fileId, { deletedBy }) {
    // Simple validation - deletedBy should be a string
    const deletedByValue = String(deletedBy);
    const url = `${BASE_API_URL}/files/${fileId}`;
    const payload = { deletedBy: deletedByValue };
    try {
        console.debug('[filesApi] DELETE', url, payload);
        // Use axios.request so body is reliably sent for DELETE
        const response = await axios.request({
            method: 'DELETE',
            url,
            data: payload,
            headers: { 'Content-Type': 'application/json' },
        });
        return response.data;
    } catch (err) {
        console.error('[filesApi] deleteFile error', err.response?.status, err.response?.data, err.message);
        throw err;
    }
}

export async function reconcileProject(projectId) {
    const response = await axios.post(`${BASE_API_URL}/admin/reconcile/${projectId}`);
    return response.data;
}