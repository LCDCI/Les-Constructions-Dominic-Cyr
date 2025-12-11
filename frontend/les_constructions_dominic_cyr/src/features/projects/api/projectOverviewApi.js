const API_BASE_URL =
  import.meta.env.VITE_API_BASE || 'http://localhost:8080/api/v1';

export const projectOverviewApi = {
  getProjectOverview: async projectIdentifier => {
    const response = await fetch(
      `${API_BASE_URL}/projects/${projectIdentifier}/overview`
    );
    if (!response.ok) {
      throw new Error('Failed to fetch project overview');
    }
    return response.json();
  },
};
