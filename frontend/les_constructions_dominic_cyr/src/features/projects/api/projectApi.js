// Use relative path to leverage Vite proxy
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api/v1';

const FILES_SERVICE_BASE_URL =
  import.meta.env.VITE_FILES_SERVICE_URL || 'http://localhost:8082';
export const projectApi = {
  getAllProjects: async (filters = {}) => {
    const params = new URLSearchParams();

    if (filters.status) params.append('status', filters.status);
    if (filters.startDate) params.append('startDate', filters.startDate);
    if (filters.endDate) params.append('endDate', filters.endDate);
    if (filters.customerId) params.append('customerId', filters.customerId);

    const queryString = params.toString();
    const url = `${API_BASE_URL}/projects${queryString ? `?${queryString}` : ''}`;

    const response = await fetch(url);
    if (!response.ok) {
      throw new Error('Failed to fetch projects');
    }
    return response.json();
  },

  getProjectById: async projectIdentifier => {
    const response = await fetch(
      `${API_BASE_URL}/projects/${projectIdentifier}`
    );
    if (!response.ok) {
      throw new Error('Failed to fetch project');
    }
    return response.json();
  },

  createProject: async (projectData, token) => {
    try {
      const headers = {
        'Content-Type': 'application/json',
      };
      if (token) {
        headers.Authorization = `Bearer ${token}`;
      }
      const response = await fetch(`${API_BASE_URL}/projects`, {
        method: 'POST',
        headers,
        body: JSON.stringify(projectData),
      });

      if (!response.ok) {
        let errorMessage = `Failed to create project (${response.status})`;
        // Read response as text first, then try to parse as JSON
        const errorText = await response.text();
        if (errorText) {
          try {
            const errorData = JSON.parse(errorText);
            if (errorData.message) {
              errorMessage = errorData.message;
            } else if (errorData.error) {
              errorMessage = errorData.error;
            } else {
              errorMessage = errorText;
            }
          } catch (e) {
            // If not valid JSON, use the text as error message
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
        throw new Error(
          'Network error: Could not connect to server. Please check if the backend is running.'
        );
      }
      throw error;
    }
  },

  updateProject: async (projectIdentifier, projectData, token) => {
    const headers = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }
    const response = await fetch(
      `${API_BASE_URL}/projects/${projectIdentifier}`,
      {
        method: 'PUT',
        headers,
        body: JSON.stringify(projectData),
      }
    );
    if (!response.ok) {
      throw new Error('Failed to update project');
    }
    return response.json();
  },

  deleteProject: async (projectIdentifier, token) => {
    const headers = {};
    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }
    const response = await fetch(
      `${API_BASE_URL}/projects/${projectIdentifier}`,
      {
        method: 'DELETE',
        headers,
      }
    );
    if (!response.ok) {
      throw new Error('Failed to delete project');
    }
  },

  uploadProjectImage: async (file, userId = 'demo-user') => {
    const formData = new FormData();
    formData.append('file', file);

    const response = await fetch(`${API_BASE_URL}/photos/upload`, {
      method: 'POST',
      headers: {
        'X-User': userId,
      },
      body: formData,
    });

    if (!response.ok) {
      throw new Error('Failed to upload image');
    }
    return response.json();
  },

  getImageUrl: imageIdentifier => {
    if (!imageIdentifier) return null;
    return `${FILES_SERVICE_BASE_URL}/files/${imageIdentifier}`;
  },
};
