import apiClient from '../../utils/axios.js';

export const reportService = {
  generateReport: async reportData => {
    const response = await apiClient.post('/reports/generate', reportData);
    return response.data;
  },
  getReports: async (page = 0, size = 10) => {
    const response = await apiClient.get('/reports', {
      params: { page, size },
    });
    return response.data;
  },
  getReport: async reportId => {
    const response = await apiClient.get(`/reports/${reportId}`);
    return response.data;
  },
  downloadReport: async reportId => {
    const response = await apiClient.get(`/reports/${reportId}/download`, {
      responseType: 'blob',
    });
    return response;
  },
  deleteReport: async reportId => {
    await apiClient.delete(`/reports/${reportId}`);
  },
};
