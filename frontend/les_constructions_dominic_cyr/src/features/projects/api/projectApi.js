const API_BASE_URL =
  import.meta.env.VITE_API_BASE || 'http://localhost:8080/api/v1';

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
    const url = `${API_BASE_URL}/projects${queryString ? `? ${queryString}` : ''}`;

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

  createProject: async projectData => {
    const response = await fetch(`${API_BASE_URL}/projects`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(projectData),
    });
    if (!response.ok) {
      throw new Error('Failed to create project');
    }
    return response.json();
  },

  updateProject: async (projectIdentifier, projectData) => {
    const response = await fetch(
      `${API_BASE_URL}/projects/${projectIdentifier}`,
      {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(projectData),
      }
    );
    if (!response.ok) {
      throw new Error('Failed to update project');
    }
    return response.json();
  },

  deleteProject: async projectIdentifier => {
    const response = await fetch(
      `${API_BASE_URL}/projects/${projectIdentifier}`,
      {
        method: 'DELETE',
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
