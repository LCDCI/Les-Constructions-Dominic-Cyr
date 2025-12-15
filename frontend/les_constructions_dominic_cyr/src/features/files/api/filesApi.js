import axios from 'axios';

const BASE_API_URL = 'http://localhost:8082'; 

export async function fetchProjectFiles(projectId) {
    const response = await axios.get(`${BASE_API_URL}/projects/${projectId}/files`); 
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
    // Ensure deletedBy is a string (extract id, email, or name if it's an object)
    let deletedByValue = deletedBy;
    if (typeof deletedBy === 'object' && deletedBy !== null) {
        if ('id' in deletedBy) {
            deletedByValue = deletedBy.id;
        } else if ('email' in deletedBy) {
            deletedByValue = deletedBy.email;
        } else if ('name' in deletedBy) {
            deletedByValue = deletedBy.name;
        } else {
            deletedByValue = JSON.stringify(deletedBy);
        }
    }
    const response = await axios({
        method: 'DELETE',
        url: `${BASE_API_URL}/files/${fileId}`,
        data: {
            deletedBy: deletedByValue
        },
        headers: {
            'Content-Type': 'application/json'
        }
    });
    return response.data;
}