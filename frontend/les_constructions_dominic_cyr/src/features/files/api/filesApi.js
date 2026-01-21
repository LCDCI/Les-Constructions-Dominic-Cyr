import axios from 'axios';

const BASE_API_URL = import.meta.env.VITE_FILES_SERVICE_URL || `${window.location.origin}/files`;

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

export async function fetchProjectDocuments(projectId, role = null, userId = null) {
    let url = `${BASE_API_URL}/projects/${projectId}/documents`;
    if (role && userId) {
        url += `?role=${encodeURIComponent(role)}&userId=${encodeURIComponent(userId)}`;
    }
    const response = await axios.get(url);
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

export async function downloadFile(fileId, fileName, role, userId) {
    // Always use role-checked endpoint if role and userId are provided
    let url = `${BASE_API_URL}/files/${fileId}/download`;
    if (role && userId) {
        url += `?role=${encodeURIComponent(role)}&userId=${encodeURIComponent(userId)}`;
    } else {
        // Fallback to regular download if no role/userId (backward compatibility)
        url = `${BASE_API_URL}/files/${fileId}`;
    }
    
    const response = await axios.get(url, {
        responseType: 'blob',
    });
    
    // Create a blob URL and trigger download
    const blob = new Blob([response.data], { type: response.headers['content-type'] || 'application/octet-stream' });
    const url_obj = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url_obj;
    link.setAttribute('download', fileName || 'document');
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url_obj);
}

export async function downloadAllFilesAsZip(projectId, role, userId, projectName) {
    let url = `${BASE_API_URL}/projects/${projectId}/documents/zip?role=${encodeURIComponent(role)}&userId=${encodeURIComponent(userId)}`;
    if (projectName) {
        url += `&projectName=${encodeURIComponent(projectName)}`;
    }
    
    const response = await axios.get(url, {
        responseType: 'blob',
    });
    
    // Create a blob URL and trigger download
    const blob = new Blob([response.data], { type: 'application/zip' });
    const url_obj = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url_obj;
    const zipFileName = projectName 
        ? `${projectName.replace(/\s+/g, '-')}-documents.zip`
        : `project-${projectId}-documents.zip`;
    link.setAttribute('download', zipFileName);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url_obj);
}