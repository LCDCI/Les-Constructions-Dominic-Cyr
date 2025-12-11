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

export async function deleteFile(fileId) {
    await axios.delete(`${BASE_API_URL}/files/${fileId}`);
}