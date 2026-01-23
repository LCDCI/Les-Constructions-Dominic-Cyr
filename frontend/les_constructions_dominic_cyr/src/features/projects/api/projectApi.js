// Use relative path to leverage Vite proxy
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api/v1';

import { getFilesServiceBase } from '../../../utils/filesService';
const FILES_SERVICE_BASE_URL = getFilesServiceBase();

export const projectApi = {
    getAllProjects: async (filters = {}, token = null) => {
        const params = new URLSearchParams();

        if (filters.status) params.append('status', filters.status);
        if (filters.startDate) params.append('startDate', filters.startDate);
        if (filters.endDate) params.append('endDate', filters.endDate);
        if (filters.customerId) params.append('customerId', filters.customerId);

        const queryString = params.toString();
        const url = `${API_BASE_URL}/projects${queryString ? `?${queryString}` : ''}`;

        const headers = {};
        if (token) {
            headers.Authorization = `Bearer ${token}`;
        }

        const response = await fetch(url, { headers });
        if (!response.ok) {
            throw new Error('Failed to fetch projects');
        }
        return response.json();
    },

    getProjectById: async (projectIdentifier, token) => {
        // 1. PROACTIVE CHECK: If no token, go straight to public overview to avoid 401 console error
        if (!token) {
            const overviewUrl = `${API_BASE_URL}/projects/${projectIdentifier}/overview`;
            const response = await fetch(overviewUrl);
            if (!response.ok) throw new Error('Failed to fetch project overview');
            return response.json();
        }

        const headers = { Authorization: `Bearer ${token}` };
        const url = `${API_BASE_URL}/projects/${projectIdentifier}`;

        try {
            const response = await fetch(url, { headers });

            if (!response.ok) {
                // 2. FALLBACK: If token is expired or unauthorized, try overview
                const overviewUrl = `${API_BASE_URL}/projects/${projectIdentifier}/overview`;
                const overviewResp = await fetch(overviewUrl);
                if (overviewResp.ok) return overviewResp.json();

                throw new Error(`Failed to fetch project (${response.status})`);
            }
            return response.json();
        } catch (error) {
            // 3. LAST RESORT FALLBACK
            const overviewUrl = `${API_BASE_URL}/projects/${projectIdentifier}/overview`;
            const lastResort = await fetch(overviewUrl);
            if (lastResort.ok) return lastResort.json();
            throw error;
        }
    },

    createProject: async (projectData, token) => {
        try {
            const headers = { 'Content-Type': 'application/json' };
            if (token) headers.Authorization = `Bearer ${token}`;

            const response = await fetch(`${API_BASE_URL}/projects`, {
                method: 'POST',
                headers,
                body: JSON.stringify(projectData),
            });

            if (!response.ok) {
                let errorMessage = `Failed to create project (${response.status})`;
                const errorText = await response.text();
                if (errorText) {
                    try {
                        const errorData = JSON.parse(errorText);
                        errorMessage = errorData.message || errorData.error || errorText;
                    } catch (e) {
                        errorMessage = errorText;
                    }
                }
                const error = new Error(errorMessage);
                error.status = response.status;
                throw error;
            }
            return response.json();
        } catch (error) {
            if (error.name === 'TypeError' && error.message.includes('fetch')) {
                throw new Error('Network error: Could not connect to server.');
            }
            throw error;
        }
    },

    updateProject: async (projectIdentifier, projectData, token) => {
        const headers = { 'Content-Type': 'application/json' };
        if (token) headers.Authorization = `Bearer ${token}`;

        const response = await fetch(`${API_BASE_URL}/projects/${projectIdentifier}`, {
            method: 'PUT',
            headers,
            body: JSON.stringify(projectData),
        });
        if (!response.ok) throw new Error('Failed to update project');
        return response.json();
    },

    deleteProject: async (projectIdentifier, token) => {
        const headers = {};
        if (token) headers.Authorization = `Bearer ${token}`;

        const response = await fetch(`${API_BASE_URL}/projects/${projectIdentifier}`, {
            method: 'DELETE',
            headers,
        });
        if (!response.ok) throw new Error('Failed to delete project');
    },

    uploadProjectImage: async (file, userId = 'demo-user') => {
        const formData = new FormData();
        formData.append('file', file);

        const response = await fetch(`${API_BASE_URL}/photos/upload`, {
            method: 'POST',
            headers: { 'X-User': userId },
            body: formData,
        });

        if (!response.ok) throw new Error('Failed to upload image');
        return response.json();
    },

    getImageUrl: (imageIdentifier) => {
        if (!imageIdentifier) return null;
        return `${FILES_SERVICE_BASE_URL}/files/${imageIdentifier}`;
    },

    assignContractorToProject: async (projectIdentifier, contractorId, token) => {
        const response = await fetch(`${API_BASE_URL}/projects/${projectIdentifier}/contractor?contractorId=${contractorId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${token}`,
            },
        });
        if (!response.ok) throw new Error('Failed to assign contractor');
        return response.json();
    },

    removeContractorFromProject: async (projectIdentifier, token) => {
        const response = await fetch(`${API_BASE_URL}/projects/${projectIdentifier}/contractor`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${token}`,
            },
        });
        if (!response.ok) throw new Error('Failed to remove contractor');
        return response.json();
    },

    assignSalespersonToProject: async (projectIdentifier, salespersonId, token) => {
        const response = await fetch(`${API_BASE_URL}/projects/${projectIdentifier}/salesperson?salespersonId=${salespersonId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${token}`,
            },
        });
        if (!response.ok) throw new Error('Failed to assign salesperson');
        return response.json();
    },

    removeSalespersonFromProject: async (projectIdentifier, token) => {
        const response = await fetch(`${API_BASE_URL}/projects/${projectIdentifier}/salesperson`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${token}`,
            },
        });
        if (!response.ok) throw new Error('Failed to remove salesperson');
        return response.json();
    },

    assignCustomerToProject: async (projectIdentifier, customerId, token) => {
        const response = await fetch(`${API_BASE_URL}/projects/${projectIdentifier}/customer?customerId=${customerId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${token}`,
            },
        });
        if (!response.ok) throw new Error('Failed to assign customer');
        return response.json();
    },

    removeCustomerFromProject: async (projectIdentifier, token) => {
        const response = await fetch(`${API_BASE_URL}/projects/${projectIdentifier}/customer`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${token}`,
            },
        });
        if (!response.ok) throw new Error('Failed to remove customer');
        return response.json();
    },

    getProjectActivityLog: async (projectIdentifier, token) => {
        const headers = {};
        if (token) headers.Authorization = `Bearer ${token}`;

        const response = await fetch(`${API_BASE_URL}/projects/${projectIdentifier}/activity-log`, { headers });
        if (!response.ok) throw new Error(`Failed to fetch activity log (${response.status})`);
        return response.json();
    },
};