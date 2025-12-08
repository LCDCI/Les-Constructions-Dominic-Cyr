import axios from 'axios';

const BASE_API_URL = 'http://localhost:8082';

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